package com.auritylab.spring.gcp.tasks.config.endpoint

import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(CloudTasksEndpointConfiguration::class)
@EnableCloudTasks
annotation class EnableCloudTasksWithEndpoint
