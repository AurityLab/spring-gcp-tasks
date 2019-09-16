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
@ContextConfiguration(classes = [CloudTasksPropertiesDefaultsTest.TestConfiguration::class])
@TestPropertySource("/test-defaults.properties")
class CloudTasksPropertiesDefaultsTest {
    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    @Test
    fun `Test properties for default values`(
        @Autowired properties: CloudTasksProperties
    ) {
        assert(properties.signatureSecret == "zAAir5p0PNFOdN7ayi63iVv1la9q6mPJ")

        assert(properties.defaultProjectId == null)
        assert(properties.defaultLocationId == null)
        assert(properties.defaultQueueId == null)

        assert(properties.defaultWorkerEndpoint == null)
        assert(properties.defaultWorkerEndpointRoute == "/taskhandler")
        assert(properties.defaultWorkerRoute == "")

        assert(!properties.skipCloudTasks)
        assert(!properties.skipTaskEndpoint)

        assert(properties.queueIdGlobalPrefix == "")
    }
}
