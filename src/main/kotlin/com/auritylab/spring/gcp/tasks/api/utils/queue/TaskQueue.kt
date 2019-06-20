package com.auritylab.spring.gcp.tasks.api.utils.queue

data class TaskQueue(val projectId: String, val locationId: String, val queueId: String) {
    override fun toString(): String = "projects/$projectId/locations/$locationId/queues/$queueId"
}
