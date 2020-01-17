package com.auritylab.spring.gcp.tasks.api

import java.util.*
import com.auritylab.spring.gcp.tasks.api.exceptions.CloudTasksNoRetryException
import com.auritylab.spring.gcp.tasks.api.exceptions.CloudTasksFailedToSubmitTaskException
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksPayloadException
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Abstract class for task worker implementations.
 */
// @Component
abstract class TaskWorker<T : Any>(private val payloadClass: KClass<T>) {
    companion object {
        internal fun runFor(worker: TaskWorker<*>, payload: String, id: UUID) {
            worker.runWorker(payload, id)
        }
    }

    @Autowired
    private lateinit var taskExecutor: TaskExecutor

    @Autowired
    private lateinit var properties: CloudTasksProperties

    @Autowired
    private lateinit var gcpProjectIdProvider: GcpProjectIdProvider

    private val gson = Gson()
    private val jacksonMapper = jacksonObjectMapper()

    private val settingsLazy = lazy {
        TaskWorkerSettings(properties, gcpProjectIdProvider,
            getCloudTaskAnnotation())
    }

    private fun getDefaultSettings(): TaskWorkerSettings = settingsLazy.value

    internal fun referencesProvider(provider: TaskProvider<*, *>) =
        provider.taskWorkerClass == this::class

    /**
     * Will return settings for this [TaskWorker] instance.
     *
     * Can be overridden. Returns generated settings by [TaskWorker]
     * instance configuration (annotation, spring properties, ...)
     * as default.
     *
     * @return Settings for this [TaskWorker] instance.
     */
    open fun getSettings(): TaskWorkerSettings = getDefaultSettings()

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
     * throw an [CloudTasksNoRetryException]. The task will be marked as
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
     * @throws CloudTasksNoRetryException If something went wrong and retry is NOT allowed
     */
    protected abstract fun run(payload: T, id: UUID)

    /**
     * Parses the given [payload] and runs this worker.
     *
     * @param payload The payload to use, as a string
     * @param id The id of the task
     * @throws Exception If something went wrong and a retry is allowed
     * @throws CloudTasksNoRetryException If something went wrong and retry is NOT allowed
     */
    @Suppress("UNCHECKED_CAST")
    private fun runWorker(payload: String, id: UUID) {
        val wrapper = try {
            jacksonMapper.readValue<PayloadWrapper<T>>(payload, jacksonMapper.typeFactory
                .constructParametricType(PayloadWrapper::class.java, payloadClass.java))
        } catch (e: IOException) {
            throw InvalidCloudTasksPayloadException("Task payload could not be deserialized to PayloadWrapper! " +
                "Maybe invalid json?", e)
        }

        // Run worker
        run(wrapper.payload, id)
    }

    /**
     * Use this method to add a task with given [payload] to
     * the queue. Returns uuid of the task.
     *
     * @param payload The payload to use for the task
     * @return The uuid of the task
     * @throws CloudTasksFailedToSubmitTaskException If something went wrong while adding task to queue
     */
    fun execute(payload: T): UUID {
        try {
            val wrapper = PayloadWrapper(payload)
            val json = gson.toJson(wrapper)

            return taskExecutor.execute(this, json)
        } catch (e: Exception) {
            throw CloudTasksFailedToSubmitTaskException("Failed to submit task to GCP Cloud Tasks!", e)
        }
    }

    private fun getCloudTaskAnnotation(): CloudTask? = this::class.findAnnotation()
}
