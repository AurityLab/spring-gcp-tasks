package com.auritylab.spring.gcp.tasks.test

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueueFactory
import java.util.*

@CloudTask
class WorkerSyntaxTest : ITaskWorker<WorkerSyntaxTest.Payload>(Payload::class) {
    override fun getQueue(): TaskQueueFactory {
        TODO("not implemented")
    }

    override fun run(payload: Payload, id: UUID) {
        TODO("not implemented")
    }

    data class Payload(val str: String, val count: Int)
}
