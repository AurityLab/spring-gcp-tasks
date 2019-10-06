package com.auritylab.spring.gcp.tasks.config.endpoint

import com.auritylab.spring.gcp.tasks.core.TaskEndpoint
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    TaskEndpoint::class
)
@ConditionalOnBean(CloudTasksEndpointConfiguration.EndpointMarker::class)
class CloudTasksEndpointAutoConfiguration
