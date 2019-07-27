package com.auritylab.spring.gcp.tasks.config

import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(CloudTasksLibraryConfiguration::class)
annotation class EnableCloudTasks
