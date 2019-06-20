package com.auritylab.spring.gcp.tasks.core.executor

import org.springframework.stereotype.Component
import java.util.*

@Component
class TaskExecutor {
    fun execute(queue: String, payload: String): UUID {
        val uuid = UUID.randomUUID()

        // ToDO: Implement

        return uuid
    }
}
