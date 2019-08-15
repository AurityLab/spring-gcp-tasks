package com.auritylab.spring.gcp.tasks.core.auto.services

import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueue
import com.auritylab.spring.gcp.tasks.core.auto.api.IAutoCreateService
import com.google.cloud.tasks.v2beta3.CloudTasksClient
import com.google.cloud.tasks.v2beta3.CreateQueueRequest
import com.google.cloud.tasks.v2beta3.Queue
import org.springframework.stereotype.Service

@Service
class AutoCreateTaskQueues : IAutoCreateService<TaskQueue> {
    // ToDo: Add support for configuration of queues (maybe via ICloudTasksQueuesConfiguration interface
    //  to be implemented as service)
    override fun handle(obj: TaskQueue) {
        CloudTasksClient.create().use {
            val queue = CreateQueueRequest.newBuilder()
                .setParent(obj.buildParent())
                .setQueue(
                    Queue.newBuilder()
                        .setName(obj.queueId)
                        .build()
                )
                .build()

            it.createQueue(queue)
        }
    }
}
