package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.google.cloud.tasks.v2beta3.CloudTasksClient
import com.google.cloud.tasks.v2beta3.HttpMethod
import com.google.cloud.tasks.v2beta3.HttpRequest
import com.google.cloud.tasks.v2beta3.Task
import com.google.protobuf.ByteString
import org.springframework.stereotype.Component
import java.util.*

@Component
class TaskExecutor {
    companion object {
        const val CLOUD_TASKS_SUB_ROUTE_HEADER = "CloudTasksRoute"
        const val CLOUD_TASKS_TASK_ID_HEADER = "CloudTasksTaskId"
    }

    fun execute(worker: ITaskWorker<*>, payload: String): UUID {
        val uuid = UUID.randomUUID()
        val queue = worker.getQueue().toString()
        //val base64payload = Base64.getEncoder().encodeToString(payload.toByteArray())

        CloudTasksClient.create().use {
            val task = Task.newBuilder()
                .setName("$queue/tasks/$uuid")
                .setHttpRequest(
                    HttpRequest.newBuilder()
                        .setHttpMethod(HttpMethod.POST) // ToDo: Maybe expose as property as well
                        .putHeaders(CLOUD_TASKS_SUB_ROUTE_HEADER, worker.getSubRoute())
                        .putHeaders(CLOUD_TASKS_TASK_ID_HEADER, uuid.toString())
                        .setUrl("${worker.getEndpoint()}${worker.getMainRoute()}")
                        .setBody(ByteString.copyFromUtf8(payload))
                        .build()
                )
                .build()

            it.createTask(queue, task)
        }

        return uuid
    }
}
