package com.auritylab.spring.gcp.tasks.core

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskEndpoint {
    @PostMapping("\${com.auritylab.spring.gcp.tasks.workerEndpointRoute:/}")
    fun workerEndpoint(payload: String) {}
}
