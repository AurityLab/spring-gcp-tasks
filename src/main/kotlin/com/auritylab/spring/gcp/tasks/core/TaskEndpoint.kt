package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class TaskEndpoint(
    private val explorer: BeanExplorer,
    private val properties: CloudTasksProperties
) {
    companion object {
        private const val USER_AGENT_HEADER = "User-Agent"
        private const val USER_AGENT_HEADER_VALUE = "Google-Cloud-Tasks"
    }

    // Default: /tasks
    // ToDo: Maybe check for "User-Agent: Google-Cloud-Tasks" as well
    @PostMapping("\${com.auritylab.spring.gcp.tasks.default-worker-endpoint-route}")
    fun workerEndpoint(
        @RequestBody payload: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_ROUTE_HEADER) route: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_ID_HEADER) uuid: String,
        @RequestHeader(value = USER_AGENT_HEADER, required = false) userAgent: String?
    ) {
        if (securityChecks()) {
            if (userAgent != USER_AGENT_HEADER_VALUE)
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden")
        }

        val worker = explorer.getWorkerByRoute(route)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND,
                "This application does not implement worker for given route!")

        TaskWorker.runFor(worker, payload, UUID.fromString(uuid))
    }

    private fun securityChecks(): Boolean {
        return properties.overrideEndpointSecurityChecks
            ?: !properties.skipTaskEndpoint && !properties.skipCloudTasks
    }
}
