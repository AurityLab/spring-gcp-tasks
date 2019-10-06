package com.auritylab.spring.gcp.tasks.config.endpoint

import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.TaskEndpoint
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableCloudTasks
@Import(
    CloudTasksLibraryAutoConfiguration::class,
    TaskEndpoint::class
)
@ConditionalOnBean(CloudTasksEndpointConfiguration.EndpointMarker::class)
class CloudTasksEndpointAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(TaskEndpoint::class)
    fun taskEndpoint(explorer: BeanExplorer, signatureHandler: TaskSignatureHandler) =
        TaskEndpoint(explorer, signatureHandler)
}
