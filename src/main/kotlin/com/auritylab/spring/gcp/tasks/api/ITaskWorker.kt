package com.auritylab.spring.gcp.tasks.api

import java.util.*
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskNoRetryException
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskFailedToSubmitException
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskInvalidQueueNameException
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueue
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueueFactory
import com.auritylab.spring.gcp.tasks.configurations.SpringGcpTasksConfigurationProperties
import com.auritylab.spring.gcp.tasks.core.executor.TaskExecutor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.stereotype.Component
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
     * Overrides queue name of [CloudTask] annotation.
     *
     * @return The name of the Cloud Task queue to use
     * @throws TaskInvalidQueueNameException If no queue name is specified
     */
    fun getQueue(): TaskQueue = getQueueFactory().of()

    /**
     * Will return the [TaskQueueFactory] instance of this worker.
     *
     * @return The [TaskQueueFactory] instance of this worker
     */
    protected fun getQueueFactory(): TaskQueueFactory {
        val annotation = this::class.findAnnotation<CloudTask>()

        var projectId = annotation?.projectId
        var locationId = annotation?.locationId
        var queueId = annotation?.queueId

        if (projectId   != null && projectId    == "$") projectId = null
        if (locationId  != null && locationId   == "$") locationId = null
        if (queueId     != null && queueId      == "$") queueId = null

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

            return taskExecutor.execute(getQueue().toString(), json)
        } catch (e: Exception) {
            throw TaskFailedToSubmitException("Failed to submit task to GCP Cloud Tasks!", e)
        }
    }
}
