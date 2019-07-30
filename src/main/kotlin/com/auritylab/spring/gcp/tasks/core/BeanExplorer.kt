package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.TaskWorker
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.util.Collections

@Component
class BeanExplorer {
    private val workers = ArrayList<TaskWorker<*>>()

    /**
     * Will return a list with all [TaskWorker] instances
     * in application.
     *
     * This list is unmodifiable.
     *
     * @return List with all [TaskWorker] instances n application
     */
    fun getWorkers(): List<TaskWorker<*>> {
        return Collections.unmodifiableList(workers)
    }

    /**
     * Will return the [TaskWorker] instance matching given
     * route, or null if not existing.
     *
     * @param route The route to use
     * @return The [TaskWorker] instance matching given route, or null
     */
    fun getWorkerByRoute(route: String): TaskWorker<*>? {
        return try {
            getWorkers().first { it.getSettings().taskRequest.workerRoute == route }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    class Processor(private val explorer: BeanExplorer) : BeanPostProcessor {
        override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
            if (bean is TaskWorker<*>)
                explorer.workers.add(bean)

            return super.postProcessBeforeInitialization(bean, beanName)
        }
    }
}
