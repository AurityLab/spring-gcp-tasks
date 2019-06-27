package com.auritylab.spring.gcp.tasks.configurations

import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(CloudTasksLibraryConfiguration::class)
annotation class EnableCloudTasks
