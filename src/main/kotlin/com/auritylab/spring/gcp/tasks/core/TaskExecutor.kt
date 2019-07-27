package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.core.properties.CloudTasksProperties
import com.google.cloud.tasks.v2beta3.CloudTasksClient
import com.google.cloud.tasks.v2beta3.HttpMethod
import com.google.cloud.tasks.v2beta3.HttpRequest
import com.google.cloud.tasks.v2beta3.Task
import com.google.protobuf.ByteString
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.*

@Component
class TaskExecutor(
    private val properties: CloudTasksProperties
) {
    companion object {
        /**
         * Header for task route.
         */
        const val CLOUD_TASKS_ROUTE_HEADER = "CloudTasksRoute"

        /**
         * Header for task id.
         */
        const val CLOUD_TASKS_ID_HEADER = "CloudTasksId"
    }

    fun execute(worker: ITaskWorker<*>, payload: String): UUID {
        val uuid = UUID.randomUUID()
        val settings = worker.getSettings()
        val queue = settings.taskQueue.build()
        // val base64payload = Base64.getEncoder().encodeToString(payload.toByteArray())

        if (properties.skipTaskEndpoint)
            return executeDirectly(worker, payload, uuid)

        CloudTasksClient.create().use {
            val task = Task.newBuilder()
                .setName("$queue/tasks/$uuid")
                .setHttpRequest(
                    HttpRequest.newBuilder()
                        .setHttpMethod(HttpMethod.POST) // ToDo: Maybe expose as property as well
                        .putHeaders(CLOUD_TASKS_ROUTE_HEADER, settings.taskRequest.workerRoute)
                        .putHeaders(CLOUD_TASKS_ID_HEADER, uuid.toString())
                        .setUrl("${settings.taskRequest.buildRequestUrl()}")
                        .setBody(ByteString.copyFromUtf8(payload))
                        .build()
                )
                .build()

            if (properties.skipCloudTasks)
                executeLocally(worker, task, uuid)
            else
                it.createTask(queue, task)
        }

        return uuid
    }

    // ToDo: Parse headers from request above (to have authentication headers, cloud tasks user agent, etc.)
    private fun executeLocally(worker: ITaskWorker<*>, task: Task, uuid: UUID): UUID {
        val body = task.httpRequest.body.toByteArray()
        val settings = worker.getSettings()
        val uri = URI(task.httpRequest.url)

        val request = java.net.http.HttpRequest.newBuilder()
            .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(body))
            .uri(uri)
            .header(CLOUD_TASKS_ROUTE_HEADER, settings.taskRequest.workerRoute)
            .header(CLOUD_TASKS_ID_HEADER, uuid.toString())
            .build()

        HttpClient.newHttpClient()
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())

        return uuid
    }

    private fun executeDirectly(worker: ITaskWorker<*>, payload: String, uuid: UUID): UUID {
        ITaskWorker.runFor(worker, payload, uuid)
        return uuid
    }
}
