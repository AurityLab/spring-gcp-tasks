package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class TaskEndpoint(
    private val explorer: BeanExplorer
) {
    // Default: /tasks
    // ToDo: Maybe check for "User-Agent: Google-Cloud-Tasks" as well
    @PostMapping("\${com.auritylab.spring.gcp.tasks.default-worker-endpoint-route}")
    fun workerEndpoint(
        @RequestBody payload: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_ROUTE_HEADER) route: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_ID_HEADER) uuid: String
    ) {
        val worker = explorer.getWorkerByRoute(route)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND,
                "This application does not implement worker for given sub route!")

        ITaskWorker.runFor(worker, payload, UUID.fromString(uuid))
    }
}
