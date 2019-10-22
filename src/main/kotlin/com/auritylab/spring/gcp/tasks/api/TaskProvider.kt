package com.auritylab.spring.gcp.tasks.api

import com.auritylab.spring.gcp.tasks.api.exceptions.CloudTasksProviderNotRegisteredException
import java.util.UUID

// @Component
abstract class TaskProvider<V : Any, T : TaskWorker<V>> {
    private var listener: ((payload: T) -> UUID)? = null

    internal fun register(m: (payload: T) -> UUID) {
        listener = m
    }

    internal fun unregister() {
        listener = null
    }

    fun execute(payload: T): UUID {
        return listener?.let { it(payload) }
            ?: throw CloudTasksProviderNotRegisteredException("Cloud tasks provider called but not registered!")
    }
}
