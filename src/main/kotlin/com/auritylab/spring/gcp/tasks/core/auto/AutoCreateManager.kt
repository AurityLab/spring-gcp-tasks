package com.auritylab.spring.gcp.tasks.core.auto

import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.auto.services.AutoCreateTaskQueues
import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Service

@Service
class AutoCreateManager(
    private val properties: CloudTasksProperties,
    private val beanExplorer: BeanExplorer,
    private val autoCreateTaskQueues: AutoCreateTaskQueues
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val handleTaskQueues = properties.autoCreateTaskQueues
        val handleSchedulerJobs = properties.autoCreateSchedulerJobs

        if (!handleTaskQueues && !handleSchedulerJobs)
            return

        val workers = beanExplorer.getWorkers()

        for (worker in workers) {
            if (handleTaskQueues)
                autoCreateTaskQueues.handle(worker.getSettings().taskQueue)
        }
    }
}
