package com.auritylab.spring.gcp.tasks.config

import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.TaskEndpoint
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [EnabledAutoConfigurationTest.TestConfiguration::class])
@TestPropertySource("/test-base.properties")
class EnabledAutoConfigurationTest {
    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    @Test
    fun `Test with auto configuration enabled`(
        @Autowired context: ApplicationContext
    ) {
        assert(context.containsBean(BeanExplorer::class.qualifiedName!!))
        assert(context.containsBean(TaskExecutor::class.qualifiedName!!))
        assert(context.containsBean(TaskEndpoint::class.qualifiedName!!))
    }
}
