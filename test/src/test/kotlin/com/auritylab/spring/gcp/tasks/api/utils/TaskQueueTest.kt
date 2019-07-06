package com.auritylab.spring.gcp.tasks.api.utils

import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueue
import org.junit.jupiter.api.Test

class TaskQueueTest {

    @Test
    fun `Test string representation of TaskQueue object`() {
        val testProjectId = "some-project"
        val testLocationId = "some-location"
        val testQueueId = "some-queue"

        assert(TaskQueue(testProjectId, testLocationId, testQueueId).toString()
                == "projects/$testProjectId/locations/$testLocationId/queues/$testQueueId")
    }

}
