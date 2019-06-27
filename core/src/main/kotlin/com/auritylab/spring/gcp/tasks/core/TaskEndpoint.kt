package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class TaskEndpoint(
    private val explorer: BeanExplorer
) {
    // Default: /tasks
    @PostMapping("\${com.auritylab.spring.gcp.tasks.workerMainRoute}")
    fun workerEndpoint(
        payload: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_SUB_ROUTE_HEADER) subRoute: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_TASK_ID_HEADER) uuid: String
    ) {
        val worker = explorer.getWorkerBySubRoute(subRoute) ?:
            throw ResponseStatusException(HttpStatus.NOT_FOUND,
                "This application does not implement worker for given sub route!")

        ITaskWorker.runFor(worker, payload, UUID.fromString(uuid))
    }
}
