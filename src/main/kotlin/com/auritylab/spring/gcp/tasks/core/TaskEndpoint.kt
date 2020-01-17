package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignature
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHelper
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
    private val signatureHelper: TaskSignatureHelper
) {

    // Default: /tasks
    // ToDo: Configure mapping differently as this doesn't work with default value given in properties file
    @PostMapping("\${com.auritylab.spring.gcp.tasks.default-worker-endpoint-route}")
    fun workerEndpoint(
        @RequestBody payload: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_ROUTE_HEADER) route: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_ID_HEADER) uuidStr: String,
        @RequestHeader(TaskExecutor.USER_AGENT_HEADER) userAgent: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_TIMESTAMP_HEADER) timestampStr: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_VERSION_HEADER) versionStr: String,
        @RequestHeader(TaskExecutor.CLOUD_TASKS_SIGNATURE_HEADER) signatureStr: String
    ) {
        // Transform data
        val uuid = UUID.fromString(uuidStr)

        // Check user agent header
        if (userAgent != TaskExecutor.USER_AGENT_HEADER_VALUE)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden")

        // Create signature data object from header data
        val signatureData = signatureHelper.createFromRequestSignatureData(
            payload = payload, cloudTasksIdHeader = uuidStr,
            cloudTasksRouteHeader = route, userAgentHeader = userAgent,
            cloudTasksTimestampHeader = timestampStr, cloudTasksVersionHeader = versionStr)

        val signature = TaskSignature(signatureData, signatureStr)

        // Verify signature
        if (!signatureHelper.getHandler().verify(signature))
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden")

        // Get worker
        val worker = explorer.getWorkerByRoute(route)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND,
                "This application does not implement worker for given route!")

        // Run worker
        TaskWorker.runFor(worker, payload, uuid)
    }
}
