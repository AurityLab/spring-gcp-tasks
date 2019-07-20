package com.auritylab.spring.gcp.tasks.api.utils.queue

import com.auritylab.spring.gcp.tasks.api.annotations.CloudTasksDsl
import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksQueueException

data class TaskQueue(val projectId: String, val locationId: String, val queueId: String) {
    fun build(): String = "projects/$projectId/locations/$locationId/queues/$queueId"

    companion object {
        /**
         * Creates new [Builder] instance and calls [Builder.build]
         * at the end.
         *
         * @param dslBuilder The builder function
         * @return The built [TaskQueue] object
         */
        operator fun invoke(dslBuilder: Builder.() -> Unit): TaskQueue {
            val builder = Builder()
            builder.dslBuilder()
            return builder.build()
        }
    }

    @CloudTasksDsl
    class Builder {
        private var projectId: String? = null
        private var locationId: String? = null
        private var queueId: String? = null

        /**
         * Set values based on old ones from given [TaskQueue] object.
         *
         * @param queue The old [TaskQueue] object to use
         * @return This [Builder] instance
         */
        fun fromTaskQueue(queue: TaskQueue): Builder {
            projectId = queue.projectId
            locationId = queue.locationId
            queueId = queue.queueId
            return this@Builder
        }

        /**
         * Set the project id for the [TaskQueue] object to build.
         *
         * @param projectId The project id to use
         * @return This [Builder] instance
         */
        fun setProjectId(projectId: String?): Builder {
            this.projectId = projectId
            return this@Builder
        }

        /**
         * Set the location id for the [TaskQueue] object to build.
         *
         * @param locationId The location id to use
         * @return This [Builder] instance
         */
        fun setLocationId(locationId: String?): Builder {
            this.locationId = locationId
            return this@Builder
        }

        /**
         * Set the queue id for the [TaskQueue] object to build.
         *
         * @param queueId The queue id to use
         * @return This [Builder] instance
         */
        fun setQueueId(queueId: String?): Builder {
            this.queueId = queueId
            return this@Builder
        }

        /**
         * Builds the [TaskQueue] object based on the data in this [Builder]
         * instance.
         *
         * Throws an exception, if [Builder.projectId], [Builder.locationId],
         * and/or [Builder.queueId] is not set.
         *
         * @return The built [TaskQueue] object
         * @throws InvalidCloudTasksQueueException If a property is not set
         */
        fun build(): TaskQueue {
            val exceptionMessage = when {
                projectId == null -> "No project id is given!"
                locationId == null -> "No location id is given!"
                queueId == null -> "No queue id is given!"
                else -> null
            }

            if (exceptionMessage != null)
                throw InvalidCloudTasksQueueException(exceptionMessage)

            return TaskQueue(projectId!!, locationId!!, queueId!!)
        }
    }
}
