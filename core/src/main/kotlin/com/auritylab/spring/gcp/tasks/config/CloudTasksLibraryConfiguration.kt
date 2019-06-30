package com.auritylab.spring.gcp.tasks.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudTasksLibraryConfiguration {
    @Bean
    fun cloudTasksLibraryBean() = Marker()

    class Marker
}
