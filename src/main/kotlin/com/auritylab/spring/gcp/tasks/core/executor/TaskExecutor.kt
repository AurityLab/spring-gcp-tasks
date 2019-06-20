package com.auritylab.spring.gcp.tasks.core.executor

import com.auritylab.spring.gcp.tasks.configurations.SpringGcpTasksConfigurationProperties
import com.auritylab.spring.gcp.tasks.remote.TaskCredentialsService
import org.springframework.stereotype.Component
import java.util.*

@Component
class TaskExecutor(
        private val properties: SpringGcpTasksConfigurationProperties,
        private val taskCredentials: TaskCredentialsService
) {
    fun execute(queue: String, payload: String): UUID {
        val uuid = UUID.randomUUID()

        return uuid
    }
}

/*CloudTasksClient.create().use {
    val task = Task.newBuilder()
            .setName("$queue/tasks/$uuid")
            .build()

    val request = CreateTaskRequest.newBuilder()
            .setParent(queue)
            .setTask(task)
            .build()

    it.createTask(request)
}*/
