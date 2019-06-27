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
     * sub route, or null if not existing.
     *
     * @return The [ITaskWorker] instance matching given sub route, or null
     */
    fun getWorkerBySubRoute(subRoute: String): ITaskWorker<*>? {
        return try {
            getWorkers().first { it.getSubRoute() == subRoute }
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
