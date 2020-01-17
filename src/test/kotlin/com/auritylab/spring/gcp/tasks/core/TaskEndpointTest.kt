package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksPayloadException
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.config.endpoint.CloudTasksEndpointAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.endpoint.EnableCloudTasksWithEndpoint
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureData
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHelper
import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@EnableCloudTasksWithEndpoint
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [TaskEndpointTest.TestConfiguration::class])
@TestPropertySource("/test-base.properties")
class TaskEndpointTest {
    companion object {
        var resultTaskId: UUID? = null
        var resultPayload: TestWorker.Payload? = null
    }

    @CloudTask(route = "test-worker")
    class TestWorker : TaskWorker<TestWorker.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {
            resultTaskId = id
            resultPayload = payload
        }

        data class Payload(val str: String)
    }

    @Configuration
    class TestConfiguration : CloudTasksEndpointAutoConfiguration() {
        @Bean
        fun testWorker(): TestWorker = TestWorker()

        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    @Test
    fun `Test TaskEndpoint with valid request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHelper: TaskSignatureHelper
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        val signatureData = createTestSignatureData(payload, id)

        callWorkerEndpoint(endpoint, signatureHelper, signatureData)

        assert(resultTaskId == id)
        assert(resultPayload == payload)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid route on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHelper: TaskSignatureHelper
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        val signatureData = createTestSignatureData(payload, id, workerRoute = "some-invalid-route")

        val exception = assertThrows<ResponseStatusException> {
            callWorkerEndpoint(endpoint, signatureHelper, signatureData)
        }

        assert(exception.status == HttpStatus.NOT_FOUND)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid user agent on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHelper: TaskSignatureHelper
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        val signatureData = createTestSignatureData(payload, id, userAgent = "some-invalid-user-agent")

        val exception = assertThrows<ResponseStatusException> {
            callWorkerEndpoint(endpoint, signatureHelper, signatureData)
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid payload json on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHelper: TaskSignatureHelper
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        // Payload needs to be in wrapper to be valid, so we just create a
        // json string of the payload without the wrapper
        val signatureData = createTestSignatureData(payload, id, payload = Gson().toJson(payload))

        val exception = assertThrows<InvalidCloudTasksPayloadException> {
            callWorkerEndpoint(endpoint, signatureHelper, signatureData)
        }

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid timestamp on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHelper: TaskSignatureHelper
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        val timestamp = (testTimestamp.toLong() - 1000).toString()
        val signatureData = createTestSignatureData(payload, id)
        val signature = signatureHelper.getHandler().sign(signatureData)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(signatureData.payload + "-test",
                signatureData.workerRoute, signatureData.taskId, signatureData.userAgent,
                timestamp, signatureData.version, signature.signature)
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid version on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHelper: TaskSignatureHelper
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        val version = (testVersion.toInt() - 1).toString()
        val signatureData = createTestSignatureData(payload, id)
        val signature = signatureHelper.getHandler().sign(signatureData)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(signatureData.payload + "-test",
                signatureData.workerRoute, signatureData.taskId, signatureData.userAgent,
                signatureData.timestamp, version, signature.signature)
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid signature on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHelper: TaskSignatureHelper
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        val signatureData = createTestSignatureData(payload, id)
        val signature = signatureHelper.getHandler().sign(signatureData)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(signatureData.payload,
                signatureData.workerRoute, signatureData.taskId, signatureData.userAgent,
                signatureData.timestamp, signatureData.version, signature.signature + "test")
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }

    private val testVersion = TaskSignatureHelper.CURRENT_VERSION.toString()
    private val testTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC).toString()

    private fun createTestSignatureData(
        defaultPayload: TestWorker.Payload,
        defaultTaskId: UUID,
        payload: String? = null,
        workerRoute: String? = null,
        taskId: String? = null,
        userAgent: String? = null,
        timestamp: String? = null,
        version: String? = null
    ) = TaskSignatureData(
        payload = payload ?: Gson().toJson(PayloadWrapper(defaultPayload)), workerRoute = workerRoute ?: "test-worker",
        taskId = taskId ?: defaultTaskId.toString(), userAgent = userAgent ?: TaskExecutor.USER_AGENT_HEADER_VALUE,
        timestamp = timestamp ?: testTimestamp, version = version ?: testVersion
    )

    private fun callWorkerEndpoint(
        endpoint: TaskEndpoint,
        signatureHelper: TaskSignatureHelper,
        signatureData: TaskSignatureData
    ) {
        val signature = signatureHelper.getHandler().sign(signatureData)

        endpoint.workerEndpoint(signatureData.payload,
            signatureData.workerRoute, signatureData.taskId, signatureData.userAgent,
            signatureData.timestamp, signatureData.version, signature.signature)
    }
}
