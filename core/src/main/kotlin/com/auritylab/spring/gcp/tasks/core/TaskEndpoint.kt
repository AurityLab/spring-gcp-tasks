package com.auritylab.spring.gcp.tasks.core

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskEndpoint {
    // ToDo: Implement multiple routes, maybe as list in properties

    @PostMapping("\${com.auritylab.spring.gcp.tasks.workerEndpointRoute:/}")
    fun workerEndpoint(payload: String) {}
}
