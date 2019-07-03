package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.google.cloud.tasks.v2beta3.CloudTasksClient
import com.google.cloud.tasks.v2beta3.Task
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
        val base64payload = Base64.getEncoder().encodeToString(payload.toByteArray())

        CloudTasksClient.create().use {
            val task = Task.newBuilder()
        }

        /*val requestBody = CreateTaskRequest().apply {
            task = Task()
                .setName("$queue/tasks/$uuid")
                .setHttpRequest(
                    HttpRequest()
                        .setHttpMethod("POST") // ToDo: Maybe expose as property as well
                        .setHeaders(mapOf(CLOUD_TASKS_SUB_ROUTE_HEADER to worker.getSubRoute()))
                        .setHeaders(mapOf(CLOUD_TASKS_TASK_ID_HEADER to uuid.toString()))
                        .setUrl("${worker.getEndpoint()}${worker.getMainRoute()}")
                        .setBody(base64payload)
                )
        }

        val request = taskCredentials.getCloudTasks()
                .projects().locations().queues().tasks().create(queue, requestBody)

        val response = request.execute()*/

        return uuid
    }
}
