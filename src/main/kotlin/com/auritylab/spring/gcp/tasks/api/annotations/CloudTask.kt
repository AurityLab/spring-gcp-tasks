package com.auritylab.spring.gcp.tasks.api.annotations

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class CloudTask(
        val projectId: String = "$",
        val locationId: String = "$",
        val queueId: String = "$",
        val customEndpoint: String = ":",
        val customRoute: String = ":"
)
