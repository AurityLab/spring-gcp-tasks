package com.auritylab.spring.gcp.tasks.core.executor

import com.auritylab.spring.gcp.tasks.configurations.SpringGcpTasksConfigurationProperties
import com.auritylab.spring.gcp.tasks.remote.TaskCredentialsService
import com.google.api.services.cloudtasks.v2beta3.model.CreateTaskRequest
import com.google.api.services.cloudtasks.v2beta3.model.HttpRequest
import com.google.api.services.cloudtasks.v2beta3.model.Task
import org.springframework.stereotype.Component
import java.util.*

@Component
class TaskExecutor(
        private val properties: SpringGcpTasksConfigurationProperties,
        private val taskCredentials: TaskCredentialsService
) {
    fun execute(queue: String, payload: String): UUID {
        val uuid = UUID.randomUUID()

        val requestBody = CreateTaskRequest().apply {
            task = Task()
                    .setName("$queue/tasks/$uuid")
                    .setHttpRequest(
                            HttpRequest()
                                    .setHttpMethod("POST")
                                    .setUrl(properties.workerEndpoint)
                                    .setBody(payload)
                    )
        }

        val request = taskCredentials.getCloudTasks()
                .projects().locations().queues().tasks().create(queue, requestBody)

        val response = request.execute()

        return uuid
    }
}
