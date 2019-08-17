package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHandler
import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
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
import kotlin.collections.ArrayList

@Component
class TaskExecutor(
    private val properties: CloudTasksProperties,
    private val signatureHandler: TaskSignatureHandler
) {
    companion object {
        /**
         * Header for user agent.
         */
        const val USER_AGENT_HEADER = "UserAgent"

        /**
         * Header value for user agent.
         */
        const val USER_AGENT_HEADER_VALUE = "Google-Cloud-Tasks"

        /**
         * Header for task route.
         */
        const val CLOUD_TASKS_ROUTE_HEADER = "CloudTasksRoute"

        /**
         * Header for task id.
         */
        const val CLOUD_TASKS_ID_HEADER = "CloudTasksId"

        /**
         * Header for task timestamp.
         */
        const val CLOUD_TASKS_TIMESTAMP_HEADER = "CloudTasksTimestamp"

        /**
         * Header for task (signature) version.
         */
        const val CLOUD_TASKS_VERSION_HEADER = "CloudTasksVersion"

        /**
         * Header for task signature.
         */
        const val CLOUD_TASKS_SIGNATURE_HEADER = "CloudTasksSignature"

        /**
         * Header for if task is scheduled.
         */
        const val CLOUD_TASKS_IS_SCHEDULED = "CloudTasksIsScheduled"
    }

    private val remoteHandler = RemoteHandler()

    fun execute(worker: TaskWorker<*>, payload: String): UUID {
        val uuid = UUID.randomUUID()
        val settings = worker.getSettings()

        val queue = settings.taskQueue.build()
        val signature = signatureHandler.sign(uuid)

        if (properties.skipTaskEndpoint)
            return executeDirectly(worker, payload, uuid)

        val task = Task.newBuilder()
            .setName("$queue/tasks/$uuid")
            .setHttpRequest(
                HttpRequest.newBuilder()
                    .setHttpMethod(HttpMethod.POST) // ToDo: Maybe expose as property as well

                    .putHeaders(CLOUD_TASKS_ROUTE_HEADER, settings.taskRequest.workerRoute)
                    .putHeaders(CLOUD_TASKS_ID_HEADER, uuid.toString())

                    .putHeaders(CLOUD_TASKS_TIMESTAMP_HEADER, signature.timestamp.toString())
                    .putHeaders(CLOUD_TASKS_VERSION_HEADER, signature.version.toString())
                    .putHeaders(CLOUD_TASKS_SIGNATURE_HEADER, signature.signature)

                    .setUrl("${settings.taskRequest.buildRequestUrl()}")
                    .setBody(ByteString.copyFromUtf8(payload))
                    .build()
            )
            .build()

            if (properties.skipCloudTasks)
                executeLocally(task, uuid)
            else
                remoteHandler.createCloudTask(queue, task)

        return uuid
    }

    private fun executeLocally(task: Task, uuid: UUID): UUID {
        val body = task.httpRequest.body.toByteArray()
        val uri = URI(task.httpRequest.url)

        // Convert map to list and add "UserAgent: Google-Cloud-Tasks"
        val headersList = headersMapToList(task.httpRequest.headersMap).apply {
            (this as ArrayList<String>).apply {
                add(USER_AGENT_HEADER)
                add(USER_AGENT_HEADER_VALUE)
            }
        }

        val request = java.net.http.HttpRequest.newBuilder()
            .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(body))
            .uri(uri)
            .headers(*headersList.toTypedArray())
            .build()

        remoteHandler.asyncHttpRequest(request, HttpResponse.BodyHandlers.ofString())

        return uuid
    }

    private fun executeDirectly(worker: TaskWorker<*>, payload: String, uuid: UUID): UUID {
        TaskWorker.runFor(worker, payload, uuid)
        return uuid
    }

    private fun headersMapToList(map: Map<String, String>): List<String> {
        val list = ArrayList<String>()

        map.forEach { entry ->
            list.add(entry.key)
            list.add(entry.value)
        }

        return list
    }

    class RemoteHandler {
        private val httpClient = HttpClient.newHttpClient()

        fun asyncHttpRequest(request: java.net.http.HttpRequest, bodyHandler: HttpResponse.BodyHandler<*>) {
            httpClient.sendAsync(request, bodyHandler)
        }

        fun createCloudTask(queue: String, task: Task) {
            CloudTasksClient.create().use {
                it.createTask(queue, task)
            }
        }
    }
}
