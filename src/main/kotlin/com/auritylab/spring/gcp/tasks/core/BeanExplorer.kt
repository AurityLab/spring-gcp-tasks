package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class BeanExplorer {
    private val workers = ArrayList<ITaskWorker<*>>()

    /**
     * Will return a list with all [ITaskWorker] instances
     * in application.
     *
     * @return List with all [ITaskWorker] instances n application
     */
    fun getWorkers(): List<ITaskWorker<*>> {
        return workers
    }

    /**
     * Will return the [ITaskWorker] instance matching given
     * route, or null if not existing.
     *
     * @param route The route to use
     * @return The [ITaskWorker] instance matching given route, or null
     */
    fun getWorkerByRoute(route: String): ITaskWorker<*>? {
        return try {
            getWorkers().first { it.getSettings().taskRequest.workerRoute == route }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    class Processor(private val explorer: BeanExplorer) : BeanPostProcessor {
        override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
            if (bean is ITaskWorker<*>)
                explorer.workers.add(bean)

            return super.postProcessBeforeInitialization(bean, beanName)
        }
    }
}