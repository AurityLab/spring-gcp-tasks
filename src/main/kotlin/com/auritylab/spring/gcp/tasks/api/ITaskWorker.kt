package com.auritylab.spring.gcp.tasks.api

import java.util.*
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskNoRetryException
import com.auritylab.spring.gcp.tasks.api.exceptions.TaskFailedToSubmitException
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask

/**
 * Interface for task worker implementations.
 */
interface ITaskWorker<T : Any> {
    /**
     * Overrides queue name of [CloudTask] annotation.
     *
     * If value is null, [CloudTask] annotation value will be used.
     *
     * @return The name of the Cloud Task queue to use
     */
    fun getQueueName(): String? = null

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
    fun run(payload: T, id: UUID)

    /**
     * Use this method to add a task with given [payload] to
     * the queue. Returns uuid of the task.
     *
     * @param payload The payload to use for the task
     * @return The uuid of the task
     * @throws TaskFailedToSubmitException If something went wrong wil adding task to queue
     */
    fun execute(payload: T): UUID {}
}
