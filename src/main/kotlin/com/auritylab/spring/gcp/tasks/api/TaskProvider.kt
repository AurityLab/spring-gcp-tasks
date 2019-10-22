package com.auritylab.spring.gcp.tasks.api

import com.auritylab.spring.gcp.tasks.api.exceptions.CloudTasksProviderNotRegisteredException
import java.util.UUID
import kotlin.reflect.KClass

// @Component
abstract class TaskProvider<V : Any, T : TaskWorker<V>>(
    internal val taskWorkerClass: KClass<T>
) {
    private var taskWorker: TaskWorker<T>? = null

    internal fun register(taskWorker: TaskWorker<*>) {
        @Suppress("UNCHECKED_CAST")
        this.taskWorker = taskWorker as TaskWorker<T>
    }

    internal fun unregister() {
        taskWorker = null
    }

    fun execute(payload: T): UUID {
        return taskWorker?.execute(payload)
            ?: throw CloudTasksProviderNotRegisteredException("Cloud tasks provider called but not registered!")
    }
}
