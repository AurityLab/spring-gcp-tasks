package com.auritylab.spring.gcp.tasks.api

import java.util.*
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskNoRetryException
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskFailedToSubmitException
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskInvalidEndpointException
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskInvalidQueueNameException
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueue
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueueFactory
import com.auritylab.spring.gcp.tasks.configurations.SpringGcpTasksConfigurationProperties
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.stereotype.Component
import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Abstract class for task worker implementations.
 */
@Component
abstract class ITaskWorker<T : Any>(private val payloadClass: KClass<T>) {
    companion object {
        internal fun runFor(worker: ITaskWorker<*>, payload: String, id: UUID) {
            worker.runWorker(payload, id)
        }

        private val mapper = jacksonObjectMapper()
    }

    @Autowired
    private lateinit var taskExecutor: TaskExecutor

    @Autowired
    private lateinit var properties: SpringGcpTasksConfigurationProperties

    @Autowired
    private lateinit var gcpProjectIdProvider: GcpProjectIdProvider

    /**
     * Will return the final queue name as a [TaskQueue] object.
     *
     * @return The queue name as a [TaskQueue] object
     * @throws TaskInvalidQueueNameException If no queue name is specified
     */
    fun getQueue(): TaskQueue = getQueueFactory().of()

    /**
     * Will return the final worker endpoint.
     *
     * If a property is null, the default is used.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties`, `spring configuration properties`
     *
     * @return The final worker endpoint
     */
    fun getEndpoint(): URL {
        val annotation = getCloudTaskAnnotation()

        var urlStr = annotation?.customEndpoint
        if (urlStr != null && urlStr == ":") urlStr = null

        return URL(urlStr ?: properties.defaultWorkerEndpoint ?:
            throw TaskInvalidEndpointException("No worker endpoint given!"))
    }

    /**
     * Will return the final worker endpoint main route.
     *
     * Represents [SpringGcpTasksConfigurationProperties.workerMainRoute].
     *
     * @return The worker endpoint main route
     */
    fun getMainRoute(): String = properties.workerMainRoute

    /**
     * Will return the final worker endpoint sub route.
     *
     * If a property is null, the default is used.
     * If that is also null, it will be set to an empty string.
     *
     * Default properties (used in order if one is null):
     * `[CloudTask] properties`, `spring configuration properties`
     *
     * @return The worker endpoint sub route
     */
    fun getSubRoute(): String {
        val annotation = getCloudTaskAnnotation()

        var routeStr = annotation?.customRoute
        if (routeStr != null && routeStr == ":") routeStr = null

        return routeStr ?: properties.defaultWorkerSubRoute
    }

    /**
     * Will return the [TaskQueueFactory] instance of this worker.
     *
     * @return The [TaskQueueFactory] instance of this worker
     */
    protected fun getQueueFactory(): TaskQueueFactory {
        val annotation = getCloudTaskAnnotation()

        var projectId = annotation?.projectId
        var locationId = annotation?.locationId
        var queueId = annotation?.queueId

        if (projectId   != null && projectId    == "$") projectId = null    // ktlint-disable
        if (locationId  != null && locationId   == "$") locationId = null   // ktlint-disable
        if (queueId     != null && queueId      == "$") queueId = null      // ktlint-disable

        return TaskQueueFactory(
                projectId ?: gcpProjectIdProvider.projectId ?: properties.defaultProjectId,
                locationId ?: properties.defaultLocationId,
                queueId ?: properties.defaultQueueId
        )
    }

    /**
     * This method gets called when the worker receives a new
     * task to process.
     *
     * The implementation should throw an exception, if something
     * went wrong. The task will then be put into the queue again
     * to retry later. The queue specifies the max number of
     * retires.
     *
     * If the task failed in a way, so that it should NOT be retried,
     * throw an [TaskNoRetryException]. The task will be marked as
     * successful in GCP Cloud Tasks to prevent retries.
     *
     * If the task succeeded by the implementation, just return
     * the function.
     *
     * This method is not designed to be called manually, but
     * usually shouldn't be a problem. No retries though.
     *
     * @param payload The payload of the task
     * @param id The id of the task
     * @throws Exception If something went wrong and a retry is allowed
     * @throws TaskNoRetryException If something went wrong and retry is NOT allowed
     */
    protected abstract fun run(payload: T, id: UUID)

    /**
     * Parses the given [payload] and runs this worker.
     *
     * @param payload The payload to use, as a string
     * @param id The id of the task
     * @throws Exception If something went wrong and a retry is allowed
     * @throws TaskNoRetryException If something went wrong and retry is NOT allowed
     */
    private fun runWorker(payload: String, id: UUID) {
        val javaType = mapper.typeFactory.constructParametricType(
                PayloadWrapper::class.java,
                payloadClass::class.java
        )
        val wrapper: PayloadWrapper<T> = mapper.readValue(payload, javaType)

        // Run worker
        run(wrapper.payload, id)
    }

    /**
     * Use this method to add a task with given [payload] to
     * the queue. Returns uuid of the task.
     *
     * @param payload The payload to use for the task
     * @return The uuid of the task
     * @throws TaskFailedToSubmitException If something went wrong while adding task to queue
     */
    fun execute(payload: T): UUID {
        try {
            val wrapper = PayloadWrapper(payload)
            val json = mapper.writeValueAsString(wrapper)

            return taskExecutor.execute(this, json)
        } catch (e: Exception) {
            throw TaskFailedToSubmitException("Failed to submit task to GCP Cloud Tasks!", e)
        }
    }

    private fun getCloudTaskAnnotation(): CloudTask? = this::class.findAnnotation()
}
