package com.auritylab.spring.gcp.tasks.api

import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueue
import com.auritylab.spring.gcp.tasks.api.utils.request.TaskRequest
import com.auritylab.spring.gcp.tasks.core.properties.CloudTasksProperties
import org.springframework.cloud.gcp.core.GcpProjectIdProvider

/**
 * Settings class for [ITaskWorker].
 */
class ITaskWorkerSettings(
    private val properties: CloudTasksProperties,
    private val gcpProjectIdProvider: GcpProjectIdProvider,
    private val annotation: CloudTask?
) {
    /**
     * Represents the [TaskRequest] object for the associated
     * [ITaskWorker] instance. Defaults to global configuration.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties`, `spring configuration properties`
     */
    var taskRequest = TaskRequest {
        var endpointStr = annotation?.endpoint
        if (endpointStr != null && endpointStr == ":") endpointStr = null
        setEndpoint(endpointStr ?: properties.defaultWorkerEndpoint)

        var endpointRouteStr = annotation?.endpointRoute
        if (endpointRouteStr != null && endpointRouteStr == ":") endpointRouteStr = null
        setEndpointRoute(endpointRouteStr ?: properties.defaultWorkerEndpointRoute)

        var routeStr = annotation?.route
        if (routeStr != null && routeStr == ":") routeStr = null
        setWorkerRoute(routeStr ?: properties.defaultWorkerRoute)
    }

    /**
     * Represents the [TaskQueue] object for the associated
     * [ITaskWorker] instance. Defaults to global configuration.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties`, `spring configuration properties`
     */
    var taskQueue = TaskQueue {
        var projectId = annotation?.projectId
        var locationId = annotation?.locationId
        var queueId = annotation?.queueId

        if (projectId   != null && projectId    == "$") projectId = null    // ktlint-disable
        if (locationId  != null && locationId   == "$") locationId = null   // ktlint-disable
        if (queueId     != null && queueId      == "$") queueId = null      // ktlint-disable

        setProjectId(projectId ?: gcpProjectIdProvider.projectId ?: properties.defaultProjectId)
        setLocationId(locationId ?: properties.defaultLocationId)
        setQueueId(queueId ?: properties.defaultQueueId)
    }

    /**
     * Updates the [TaskRequest] object of this [ITaskWorkerSettings]
     * instance with given builder function.
     *
     * Uses values from old [TaskRequest] object if not overridden.
     */
    fun updateTaskRequest(dslBuilder: TaskRequest.Builder.() -> Unit) {
        val builder = TaskRequest.Builder()
        builder.fromTaskRequest(taskRequest)

        builder.dslBuilder()
        taskRequest = builder.build()
    }

    /**
     * Updates the [TaskQueue] object of this [ITaskWorkerSettings]
     * instance with given builder function.
     *
     * Uses values from old [TaskQueue] object if not overridden.
     */
    fun updateTaskQueue(dslBuilder: TaskQueue.Builder.() -> Unit) {
        val builder = TaskQueue.Builder()
        builder.fromTaskQueue(taskQueue)

        builder.dslBuilder()
        taskQueue = builder.build()
    }
}
