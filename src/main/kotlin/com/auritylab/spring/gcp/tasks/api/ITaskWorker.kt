package com.auritylab.spring.gcp.tasks.api

import java.util.*
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskNoRetryException
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskFailedToSubmitException
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskNoQueueNameException
import com.auritylab.spring.gcp.tasks.api.payload.PayloadWrapper
import com.auritylab.spring.gcp.tasks.executor.TaskExecutor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.reflect.full.findAnnotation

/**
 * Abstract class for task worker implementations.
 */
@Component
abstract class ITaskWorker<T : Any> {
    companion object {
        fun <T : Any> runFor(worker: ITaskWorker<T>, payload: T, id: UUID) {
            worker.run(payload, id)
        }

        private val mapper = jacksonObjectMapper()
    }

    @Autowired
    private lateinit var taskExecutor: TaskExecutor

    /**
     * Overrides queue name of [CloudTask] annotation.
     *
     * @return The name of the Cloud Task queue to use
     * @throws TaskNoQueueNameException If no queue name is specified
     */
    fun getQueueName(): String {
        try {
            return this::class.findAnnotation<CloudTask>()!!.queue
        } catch (e: NullPointerException) {
            throw TaskNoQueueNameException("Queue name is not specified by annotation or overridden!", e)
        }
    }

    /**
     * This method gets called when the worker receives a new
     * task to process.
     *
     * The implementation should throw an exception, if something
     * went wrong. The task will then be put into the queue again
     * to retry later. The task itself specifies the max number of
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

            return taskExecutor.execute(getQueueName(), json)
        } catch (e: Exception) {
            throw TaskFailedToSubmitException("Failed to submit task to GCP Cloud Tasks!", e)
        }
    }
}
