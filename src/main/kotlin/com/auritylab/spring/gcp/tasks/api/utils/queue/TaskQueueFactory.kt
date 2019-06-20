package com.auritylab.spring.gcp.tasks.api.utils.queue

import com.auritylab.spring.gcp.tasks.api.exceptions.TaskInvalidQueueNameException
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueue
import com.auritylab.spring.gcp.tasks.configurations.SpringGcpTasksConfigurationProperties
import org.springframework.stereotype.Component

@Component
class TaskQueueFactory(
        private val properties: SpringGcpTasksConfigurationProperties
) {
    /**
     * Creates a [TaskQueue] object based on default properties.
     *
     * If at least one default is null, an exception will be thrown.
     *
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If not all properties are given as default
     */
    fun of(): TaskQueue = of(null, null, null)

    /**
     * Creates a [TaskQueue] object based on given properties.
     *
     * If a property is null, the spring configured default is used.
     * If that is also null, an exception will be thrown.
     *
     * @param queueId The queue id of the queue, or null for default
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If one or more properties are null and no default is given
     */
    fun of(queueId: String? = null): TaskQueue = of(null, null, queueId)

    /**
     * Creates a [TaskQueue] object based on given properties.
     *
     * If a property is null, the spring configured default is used.
     * If that is also null, an exception will be thrown.
     *
     * @param locationId The location id of the queue, or null for default
     * @param queueId The queue id of the queue, or null for default
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If one or more properties are null and no default is given
     */
    fun of(locationId: String? = null, queueId: String? = null): TaskQueue = of(null, locationId, queueId)

    /**
     * Creates a [TaskQueue] object based on given properties.
     *
     * If a property is null, the spring configured default is used.
     * If that is also null, an exception will be thrown.
     *
     * @param projectId The project id of the queue, or null for default
     * @param locationId The location id of the queue, or null for default
     * @param queueId The queue id of the queue, or null for default
     * @return The constructed [TaskQueue] object
     * @throws TaskInvalidQueueNameException If one or more properties are null and no default is given
     */
    fun of(projectId: String? = null, locationId: String? = null, queueId: String? = null): TaskQueue {
        return TaskQueue(
                projectId ?: properties.defaultProjectId
                ?: throw TaskInvalidQueueNameException("No project id is given!"),
                locationId ?: properties.defaultLocationId
                ?: throw TaskInvalidQueueNameException("No location id is given!"),
                queueId ?: properties.defaultLocationId ?: throw TaskInvalidQueueNameException("No queue id is given!")
        )
    }
}
