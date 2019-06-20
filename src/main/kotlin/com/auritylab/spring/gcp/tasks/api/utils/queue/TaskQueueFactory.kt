package com.auritylab.spring.gcp.tasks.api.utils.queue

import com.auritylab.spring.gcp.tasks.api.exceptions.TaskInvalidQueueNameException
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import org.springframework.cloud.gcp.core.GcpProjectIdProvider

class TaskQueueFactory(
        private val defaultProjectId: String?,
        private val defaultLocationId: String?,
        private val defaultQueueId: String?
) {
    /**
     * Creates a [TaskQueue] object based on default properties.
     *
     * If at least one default is null, an exception will be thrown.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties", `spring configuration properties`
     *
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If not all properties are given as default
     */
    fun of(): TaskQueue = of(null, null, null)

    /**
     * Creates a [TaskQueue] object based on given properties.
     *
     * If a property is null, the default is used.
     * If that is also null, an exception will be thrown.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties`, `spring configuration properties`
     *
     * @param queueId The queue id of the queue, or null for default
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If one or more properties are null and no default is given
     */
    fun of(queueId: String?): TaskQueue = of(null, null, queueId)

    /**
     * Creates a [TaskQueue] object based on given properties.
     *
     * If a property is null, the default is used.
     * If that is also null, an exception will be thrown.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties`, `spring configuration properties`
     *
     * @param locationId The location id of the queue, or null for default
     * @param queueId The queue id of the queue, or null for default
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If one or more properties are null and no default is given
     */
    fun of(locationId: String?, queueId: String?): TaskQueue = of(null, locationId, queueId)

    /**
     * Creates a [TaskQueue] object based on given properties.
     *
     * If a property is null, the spring configured default is used.
     * If that is also null, an exception will be thrown.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties`, `spring configuration properties`
     *
     * Project id also checks for [GcpProjectIdProvider]:
     * `[CloudTask] properties`, `spring configured [GcpProjectIdProvider]`,
     * `spring configuration properties`
     *
     * @param projectId The project id of the queue, or null for default
     * @param locationId The location id of the queue, or null for default
     * @param queueId The queue id of the queue, or null for default
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If one or more properties are null and no default is given
     */
    fun of(projectId: String?, locationId: String?, queueId: String?): TaskQueue {
        return TaskQueue(
                projectId ?: defaultProjectId ?: throw TaskInvalidQueueNameException("No project id is given!"),
                locationId ?: defaultLocationId ?: throw TaskInvalidQueueNameException("No location id is given!"),
                queueId ?: defaultQueueId ?: throw TaskInvalidQueueNameException("No queue id is given!")
        )
    }
}
