package com.auritylab.spring.test.gcp.tasks.scheduler

import com.auritylab.spring.test.gcp.tasks.tasks.PrintLineTask
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PrintLineScheduler(
    private val printLineTask: PrintLineTask
) {
    init {
        println("Test")
    }

    @Scheduled(fixedDelay = 180000, initialDelay = 5000)
    fun run() {
        println("Sending task")
        printLineTask.execute(PrintLineTask.Payload("Some string", 1))
    }
}
