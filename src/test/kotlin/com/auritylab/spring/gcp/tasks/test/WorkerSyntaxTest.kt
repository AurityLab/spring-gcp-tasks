package com.auritylab.spring.gcp.tasks.test

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import java.util.*

@CloudTask(locationId = "europe-west1", queueId = "test-queue-1")
class WorkerSyntaxTest : ITaskWorker<WorkerSyntaxTest.Payload>(Payload::class) {
    override fun run(payload: Payload, id: UUID) {
        TODO("not implemented")
    }

    data class Payload(val str: String, val count: Int)
}
