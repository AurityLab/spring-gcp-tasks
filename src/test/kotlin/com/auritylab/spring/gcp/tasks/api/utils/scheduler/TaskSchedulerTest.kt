package com.auritylab.spring.gcp.tasks.api.utils.scheduler

import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksSchedulerException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TaskSchedulerTest {
    companion object {
        fun checkSchedulerObject(cron: String, scheduler: TaskScheduler) = scheduler.cron == cron
    }

    @Test
    fun `Test TaskQueue object`() {
        val testCron = "0 0 * * *"

        assert(checkSchedulerObject(testCron, TaskScheduler(testCron)))
    }

    @Test
    fun `Test builder of TaskQueue class`() {
        val testCron = "0 0 * * *"

        val scheduler = TaskScheduler {
            setCron(testCron)
        }

        assert(checkSchedulerObject(testCron, scheduler))
    }

    @Test
    fun `Test builder of TaskQueue class with existing TaskQueue object as base`() {
        val testCron = "0 0 * * *"

        val testCron2 = "0 * * * *"

        val scheduler = TaskScheduler {
            setCron(testCron)
        }

        assert(checkSchedulerObject(testCron, scheduler))

        assert(checkSchedulerObject(testCron, TaskScheduler {
            fromTaskScheduler(scheduler)
        }))

        assert(checkSchedulerObject(testCron2, TaskScheduler {
            fromTaskScheduler(scheduler)
            setCron(testCron2)
        }))
    }

    @Test
    fun `Test builder of TaskQueue class with missing values`() {
        assertThrows<InvalidCloudTasksSchedulerException> {
            TaskScheduler { }
        }
    }
}
