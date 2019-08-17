package com.auritylab.spring.gcp.tasks.api.utils.scheduler

import com.auritylab.spring.gcp.tasks.api.annotations.dsl.CloudTasksDsl
import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksSchedulerException

data class TaskScheduler(val cron: String) {
    companion object {
        /**
         * Creates new [Builder] instance and calls [Builder.build]
         * at the end.
         *
         * @param dslBuilder The builder function
         * @return The built [TaskScheduler] object
         */
        operator fun invoke(dslBuilder: Builder.() -> Unit): TaskScheduler {
            val builder = Builder()
            builder.dslBuilder()
            return builder.build()
        }
    }

    @CloudTasksDsl
    class Builder {
        private var cron: String? = null

        /**
         * Set values based on old ones from given [TaskScheduler] object.
         *
         * @param scheduler The old [TaskScheduler] object to use
         * @return This [Builder] instance
         */
        fun fromTaskScheduler(scheduler: TaskScheduler): Builder {
            cron = scheduler.cron
            return this@Builder
        }

        /**
         * Set the cron value for the [TaskScheduler] object to build.
         *
         * @param cron The cron value to use
         * @return This [Builder] instance
         */
        fun setCron(cron: String?): Builder {
            this.cron = cron
            return this@Builder
        }

        /**
         * Builds the [TaskScheduler] object based on the data in this [Builder]
         * instance.
         *
         * Throws an exception, if [Builder.cron] is not set.
         *
         * @return The built [TaskScheduler] object
         * @throws InvalidCloudTasksSchedulerException If a property is not set
         */
        fun build(): TaskScheduler {
            val exceptionMessage = when {
                cron == null -> "No cron value is given!"
                else -> null
            }

            if (exceptionMessage != null)
                throw InvalidCloudTasksSchedulerException(exceptionMessage)

            return TaskScheduler(cron!!)
        }
    }
}
