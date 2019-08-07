package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignature
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHandler
import com.auritylab.spring.gcp.tasks.utils.ByteArraySubscriber
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.internal.util.reflection.FieldSetter
import org.mockito.stubbing.Answer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.Exception
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [TaskExecutorLocalTest.TestConfiguration::class])
@TestPropertySource("/test-local.properties")
class TaskExecutorLocalTest {
    @CloudTask(route = "test-worker-1")
    class TestWorker : TaskWorker<TestWorker.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {}
        data class Payload(val str: String)
    }

    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean
        fun testWorker(): TestWorker = TestWorker()

        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    @Test
    fun `Test local task execution (with mocked http request)`(
        @Autowired testWorker: TestWorker,
        @Autowired taskExecutor: TaskExecutor,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val settings = testWorker.getSettings()

        val payload = TestWorker.Payload("test-str")
        var generatedId: String? = null

        var timestamp: Long? = null
        var version: Int? = null
        var signatureStr: String? = null

        fun mockedAsyncHttpRequest(request: HttpRequest, bodyHandler: HttpResponse.BodyHandler<*>): Void? {
            request.let {
                assert(it.method() == "POST")
                assert(it.uri().toString() == settings.taskRequest.buildRequestUrl().toString())

                it.headers().map().forEach { entry ->
                    fun checkHeader(values: List<String>): String {
                        assert(values.size == 1)
                        return values[0]
                    }

                    when (entry.key) {
                        TaskExecutor.USER_AGENT_HEADER ->
                            assert(checkHeader(entry.value) == "Google-Cloud-Tasks")

                        TaskExecutor.CLOUD_TASKS_ROUTE_HEADER ->
                            assert(checkHeader(entry.value) == settings.taskRequest.workerRoute)

                        TaskExecutor.CLOUD_TASKS_ID_HEADER ->
                            generatedId = checkHeader(entry.value)

                        TaskExecutor.CLOUD_TASKS_TIMESTAMP_HEADER ->
                            timestamp = checkHeader(entry.value).toLong()

                        TaskExecutor.CLOUD_TASKS_VERSION_HEADER ->
                            version = checkHeader(entry.value).toInt()

                        TaskExecutor.CLOUD_TASKS_SIGNATURE_HEADER ->
                            signatureStr = checkHeader(entry.value)

                        else -> throw Exception("Undefined header for TaskExecutor local http request: ${entry.key}")
                    }
                }

                val body = request.bodyPublisher().map { p ->
                    val bodySub = HttpResponse.BodySubscribers.ofByteArray()
                    val flowSub = ByteArraySubscriber(bodySub)

                    p.subscribe(flowSub)
                    return@map bodySub.body.toCompletableFuture().join()
                }.get()

                val bodyStr = ByteString.copyFrom(body).toStringUtf8()

                val token = TypeToken.getParameterized(
                    PayloadWrapper::class.java,
                    TestWorker.Payload::class.java
                )

                @Suppress("UNCHECKED_CAST")
                val wrapper =
                    Gson().getAdapter(token).fromJson(bodyStr) as PayloadWrapper<TestWorker.Payload>

                assert(wrapper.payload == payload)
            }

            assert(bodyHandler == HttpResponse.BodyHandlers.ofString())

            return null
        }

        Mockito.mock(TaskExecutor.RemoteHandler::class.java, Answer {
            return@Answer when {
                it.method.name == "asyncHttpRequest" ->
                    mockedAsyncHttpRequest(it.getArgument(0), it.getArgument(1))

                it.method.name == "createCloudTask" ->
                    throw Exception("TaskExecutor tried to execute remoteHandler.createCloudTask(...) although " +
                        "this shouldn't happen in this context!")

                else ->
                    Mockito.RETURNS_DEFAULTS.answer(it)
            }
        }).also {
            FieldSetter.setField(taskExecutor, taskExecutor.javaClass.getDeclaredField("remoteHandler"), it)
        }

        val id = testWorker.execute(payload)

        assert(id.toString() == generatedId)

        val signature = TaskSignature(signatureStr!!, timestamp!!, version!!)
        assert(signatureHandler.verify(id, signature))
    }
}
