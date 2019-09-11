package com.auritylab.spring.gcp.tasks.core.auto.services

import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueue
import com.auritylab.spring.gcp.tasks.api.utils.request.TaskRequest
import com.auritylab.spring.gcp.tasks.core.auto.api.IAutoCreateService
import org.springframework.stereotype.Service

@Service
class AutoCreateSchedulerJobs : IAutoCreateService<AutoCreateSchedulerJobs.HandlerPayload> {
    // ToDo: Add support for configuration of scheduler jobs (maybe via ICloudTasksSchedulersConfiguration interface
    //  to be implemented as service)

    override fun handle(obj: HandlerPayload) {
        TODO("not implemented")
    }

    class HandlerPayload(val queue: TaskQueue, val request: TaskRequest)
}
