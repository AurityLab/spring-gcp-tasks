package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksPayloadException
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
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
    class TestWorker : ITaskWorker<TestWorker.Payload>(Payload::class) {
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
        @Autowired endpoint: TaskEndpoint
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        endpoint.workerEndpoint(Gson().toJson(wrapper),
            "test-worker", id.toString())

        assert(resultTaskId == id)
        assert(resultPayload == payload)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid route on request`(
        @Autowired endpoint: TaskEndpoint
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")
        val wrapper = PayloadWrapper(payload)

        val exception = assertThrows<ResponseStatusException> {
            endpoint.workerEndpoint(Gson().toJson(wrapper),
                "test-worker-invalid", id.toString())
        }

        assert(exception.status == HttpStatus.NOT_FOUND)

        resultTaskId = null
        resultPayload = null
    }

    @Test
    fun `Test TaskEndpoint with invalid payload json on request`(
        @Autowired endpoint: TaskEndpoint
    ) {
        val id = UUID.randomUUID()
        val payload = TestWorker.Payload("test")

        // Payload has to be in wrapper, so we just use the payload itself,
        // which leads to an invalid payload when parsing
        assertThrows<InvalidCloudTasksPayloadException> {
            endpoint.workerEndpoint(Gson().toJson(payload),
                "test-worker", id.toString())
        }

        resultTaskId = null
        resultPayload = null
    }
}
