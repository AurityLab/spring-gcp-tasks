package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksPayloadException
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHandler
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
import java.util.UUID

@EnableCloudTasks
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
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean
        fun testWorker(): TestWorker = TestWorker()

        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    @Test
    fun `Test TaskEndpoint with valid request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        val signature = signatureHandler.sign(id)

        endpoint.workerEndpoint(Gson().toJson(wrapper),
            "test-worker", id.toString(), "Google-Cloud-Tasks",
            signature.timestamp.toString(), signature.version.toString(), signature.signature, false)

        assert(resultTaskId == id)
        assert(resultPayload == payload)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid route on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        val signature = signatureHandler.sign(id)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(Gson().toJson(wrapper),
                "test-worker-invalid", id.toString(), "Google-Cloud-Tasks",
                signature.timestamp.toString(), signature.version.toString(), signature.signature, false)
        }

        assert(exception.status == HttpStatus.NOT_FOUND)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid user agent on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        val signature = signatureHandler.sign(id)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(Gson().toJson(wrapper),
                "test-worker", id.toString(), "invalid-user-agent",
                signature.timestamp.toString(), signature.version.toString(), signature.signature, false)
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid payload json on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        val signature = signatureHandler.sign(id)

        // Payload has to be in wrapper, so we just use the payload itself,
        // which leads to an invalid payload when parsing
        assertThrows<InvalidCloudTasksPayloadException> {
            endpoint.workerEndpoint(Gson().toJson(payload),
                "test-worker", id.toString(), "Google-Cloud-Tasks",
                signature.timestamp.toString(), signature.version.toString(), signature.signature, false)
        }

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid timestamp on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        val signature = signatureHandler.sign(id)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(Gson().toJson(wrapper),
                "test-worker", id.toString(), "Google-Cloud-Tasks",
                (signature.timestamp - 1000).toString(), signature.version.toString(), signature.signature, false)
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid version on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        val signature = signatureHandler.sign(id)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(Gson().toJson(wrapper),
                "test-worker", id.toString(), "Google-Cloud-Tasks",
                signature.timestamp.toString(), (signature.version - 5).toString(), signature.signature, false)
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid signature on request`(
        @Autowired endpoint: TaskEndpoint,
        @Autowired signatureHandler: TaskSignatureHandler
    ) {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()

        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        val signature = signatureHandler.sign(id2)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(Gson().toJson(wrapper),
                "test-worker", id1.toString(), "Google-Cloud-Tasks",
                signature.timestamp.toString(), signature.version.toString(), signature.signature, false)
        }

        assert(exception.status == HttpStatus.FORBIDDEN)

        resultTaskId = null
        resultPayload = null
    }
}
