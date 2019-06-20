package com.auritylab.spring.gcp.tasks.api.utils.queue

import com.google.cloud.tasks.v2.QueueName

data class TaskQueue(val projectId: String, val locationId: String, val queueId: String) {
    override fun toString(): String = QueueName.of(projectId, locationId, queueId).toString()
}
