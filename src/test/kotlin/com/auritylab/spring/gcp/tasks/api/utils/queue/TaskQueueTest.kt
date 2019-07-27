package com.auritylab.spring.gcp.tasks.api.utils.queue

import org.junit.jupiter.api.Test

class TaskQueueTest {

    private fun checkQueueObject(projectId: String, locationId: String, queueId: String, queue: TaskQueue) =
        queue.build() == "projects/$projectId/locations/$locationId/queues/$queueId"

    @Test
    fun `Test TaskQueue object`() {
        val testProjectId = "some-project"
        val testLocationId = "some-location"
        val testQueueId = "some-queue"

        assert(checkQueueObject(testProjectId, testLocationId, testQueueId,
            TaskQueue(testProjectId, testLocationId, testQueueId)))
    }

    @Test
    fun `Test builder of TaskQueue class`() {
        val testProjectId = "some-project"
        val testLocationId = "some-location"
        val testQueueId = "some-queue"

        val queue = TaskQueue {
            setProjectId(testProjectId)
            setLocationId(testLocationId)
            setQueueId(testQueueId)
        }

        assert(checkQueueObject(testProjectId, testLocationId, testQueueId, queue))
    }
}
