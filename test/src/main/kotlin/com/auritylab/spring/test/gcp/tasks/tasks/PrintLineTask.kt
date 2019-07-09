package com.auritylab.spring.test.gcp.tasks.tasks

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import kotlinx.serialization.Serializable
import java.util.UUID

@CloudTask(customRoute = "/test/print/line")
class PrintLineTask : ITaskWorker<PrintLineTask.Payload>(Payload::class) {
    override fun run(payload: Payload, id: UUID) {
        println("${payload.count}: ${payload.str}")
    }

    @Serializable
    data class Payload(val str: String, val count: Int)
}
