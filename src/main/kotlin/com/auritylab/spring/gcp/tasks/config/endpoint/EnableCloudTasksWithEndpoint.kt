package com.auritylab.spring.gcp.tasks.config.endpoint

import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(CloudTasksEndpointConfiguration::class)
annotation class EnableCloudTasksWithEndpoint
