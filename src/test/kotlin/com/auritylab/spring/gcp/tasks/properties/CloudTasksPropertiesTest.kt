package com.auritylab.spring.gcp.tasks.properties

import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [CloudTasksPropertiesTest.TestConfiguration::class])
@TestPropertySource("/test-base.properties")
class CloudTasksPropertiesTest {
    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    @Test
    fun `Test properties for configured values`(
        @Autowired properties: CloudTasksProperties
    ) {
        assert(properties.signatureSecret == "zAAir5p0PNFOdN7ayi63iVv1la9q6mPJ")
        assert(!properties.autoCreateTaskQueues)
        assert(!properties.autoCreateSchedulerJobs)

        assert(properties.defaultProjectId == "some-project")
        assert(properties.defaultLocationId == "europe-west1")
        assert(properties.defaultQueueId == "some-queue")

        assert(properties.defaultWorkerEndpoint == "http://127.0.0.1:3000")
        assert(properties.defaultWorkerEndpointRoute == "/taskhandler/test")
        assert(properties.defaultWorkerRoute == "/test")

        assert(properties.defaultSchedulerCronValue == "0 0 * * *")

        assert(properties.skipCloudTasks)
        assert(properties.skipTaskEndpoint)
    }
}
