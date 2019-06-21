package com.auritylab.spring.gcp.tasks.core.executor

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.configurations.SpringGcpTasksConfigurationProperties
import com.auritylab.spring.gcp.tasks.remote.TaskCredentialsService
import com.google.api.services.cloudtasks.v2beta3.model.CreateTaskRequest
import com.google.api.services.cloudtasks.v2beta3.model.HttpRequest
import com.google.api.services.cloudtasks.v2beta3.model.Task
import org.springframework.stereotype.Component
import java.util.*

@Component
class TaskExecutor(
        private val taskCredentials: TaskCredentialsService
) {
    fun execute(worker: ITaskWorker<*>, payload: String): UUID {
        val uuid = UUID.randomUUID()
        val queue = worker.getQueue().toString()

        val requestBody = CreateTaskRequest().apply {
            task = Task()
                    .setName("$queue/tasks/$uuid")
                    .setHttpRequest(
                            HttpRequest()
                                    .setHttpMethod("POST") // ToDo: Maybe expose as property as well
                                    .setUrl("${worker.getEndpoint()}${worker.getRoute()}")
                                    .setBody(payload)
                    )
        }

        val request = taskCredentials.getCloudTasks()
                .projects().locations().queues().tasks().create(queue, requestBody)

        val response = request.execute()

        return uuid
    }
}
