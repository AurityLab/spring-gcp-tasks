package com.auritylab.spring.gcp.tasks.config.endpoint

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudTasksEndpointConfiguration {
    @Bean
    fun cloudTasksEndpointBean() = EndpointMarker()

    class EndpointMarker
}
