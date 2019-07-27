package com.auritylab.spring.gcp.tasks.test

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SerializationTest.TestConfiguration::class])
@TestPropertySource("/test-base.properties")
class SerializationTest {
    companion object {
        private var result: String? = null
    }

    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean
        fun testWorker(): TestWorker = TestWorker()

        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project" }
    }

    @CloudTask(projectId = "some-project", locationId = "europe-west1", queueId = "some-queue")
    class TestWorker : ITaskWorker<TestWorker.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {
            result = payload.str
        }

        @Serializable
        data class Payload(val str: String)
    }

    @Test
    fun `Test serialization of payload`(
        @Autowired worker: TestWorker
    ) {
        val str = "test"

        worker.execute(TestWorker.Payload(str))

        assert(result == str)
    }
}
