package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignature
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHandler
import com.google.cloud.tasks.v2beta3.HttpMethod
import com.google.cloud.tasks.v2beta3.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import java.util.UUID

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [TaskExecutorCloudTasksTest.TestConfiguration::class])
@TestPropertySource("/test-cloud-tasks.properties")
class TaskExecutorCloudTasksTest {
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
    fun `Test cloud tasks task execution (with mocked cloud tasks request)`(
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

        fun mockedCreateCloudTask(queue: String, task: Task): Void? {
            assert(queue == settings.taskQueue.build())

            task.let {
                assert(it.hasHttpRequest())

                it.httpRequest.let { req ->
                    assert(req.httpMethod == HttpMethod.POST)
                    assert(req.url == settings.taskRequest.buildRequestUrl().toString())

                    req.headersMap.forEach { entry ->
                        when (entry.key) {
                            TaskExecutor.USER_AGENT_HEADER ->
                                assert(entry.value == "Google-Cloud-Tasks")

                            TaskExecutor.CLOUD_TASKS_ROUTE_HEADER ->
                                assert(entry.value == settings.taskRequest.workerRoute)

                            TaskExecutor.CLOUD_TASKS_ID_HEADER ->
                                generatedId = entry.value

                            TaskExecutor.CLOUD_TASKS_TIMESTAMP_HEADER ->
                                timestamp = entry.value.toLong()

                            TaskExecutor.CLOUD_TASKS_VERSION_HEADER ->
                                version = entry.value.toInt()

                            TaskExecutor.CLOUD_TASKS_SIGNATURE_HEADER ->
                                signatureStr = entry.value

                            else -> throw Exception("Undefined header for TaskExecutor cloud tasks " +
                                "request: ${entry.key}")
                        }
                    }

                    val bodyStr = req.body.toStringUtf8()

                    val token = TypeToken.getParameterized(
                        PayloadWrapper::class.java,
                        TestWorker.Payload::class.java
                    )

                    @Suppress("UNCHECKED_CAST")
                    val wrapper =
                        Gson().getAdapter(token).fromJson(bodyStr) as PayloadWrapper<TestWorker.Payload>

                    assert(wrapper.payload == payload)
                }

                assert(it.name == "$queue/tasks/$generatedId")
            }

            return null
        }

        Mockito.mock(TaskExecutor.RemoteHandler::class.java, Answer {
            return@Answer when {
                it.method.name == "asyncHttpRequest" ->
                    throw Exception("TaskExecutor tried to execute remoteHandler.asyncHttpRequest(...) although " +
                        "this shouldn't happen in this context!")

                it.method.name == "createCloudTask" ->
                    mockedCreateCloudTask(it.getArgument(0), it.getArgument(1))

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
