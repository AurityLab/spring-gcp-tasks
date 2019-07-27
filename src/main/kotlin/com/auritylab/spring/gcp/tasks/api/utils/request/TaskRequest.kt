package com.auritylab.spring.gcp.tasks.api.utils.request

import com.auritylab.spring.gcp.tasks.api.annotations.CloudTasksDsl
import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksRequestException
import java.net.URL

data class TaskRequest(val endpoint: String, val endpointRoute: String, val workerRoute: String) {
    fun buildRequestUrl(): URL = URL(endpoint + endpointRoute)

    companion object {
        /**
         * Creates new [Builder] instance and calls [Builder.build]
         * at the end.
         *
         * @param dslBuilder The builder function
         * @return The built [TaskRequest] object
         */
        operator fun invoke(dslBuilder: Builder.() -> Unit): TaskRequest {
            val builder = Builder()
            builder.dslBuilder()
            return builder.build()
        }
    }

    @CloudTasksDsl
    class Builder {
        private var endpoint: String? = null
        private var endpointRoute: String? = null
        private var workerRoute: String? = null

        /**
         * Set values based on old ones from given [TaskRequest] object.
         *
         * @param request The old [TaskRequest] object to use
         * @return This [Builder] instance
         */
        fun fromTaskRequest(request: TaskRequest): Builder {
            endpoint = request.endpoint
            endpointRoute = request.endpointRoute
            workerRoute = request.workerRoute
            return this@Builder
        }

        /**
         * Set the endpoint for the [TaskRequest] object to build.
         *
         * @param endpoint The endpoint to use
         * @return This [Builder] instance
         */
        fun setEndpoint(endpoint: String?): Builder {
            this.endpoint = endpoint
            return this@Builder
        }

        /**
         * Set the endpoint route for the [TaskRequest] object to build.
         *
         * @param endpointRoute The endpoint route to use
         * @return This [Builder] instance
         */
        fun setEndpointRoute(endpointRoute: String?): Builder {
            this.endpointRoute = endpointRoute
            return this@Builder
        }

        /**
         * Set the worker route id for the [TaskRequest] object to build.
         *
         * @param workerRoute The worker route to use
         * @return This [Builder] instance
         */
        fun setWorkerRoute(workerRoute: String?): Builder {
            this.workerRoute = workerRoute
            return this@Builder
        }

        /**
         * Builds the [TaskRequest] object based on the data in this [Builder]
         * instance.
         *
         * Throws an exception, if [Builder.endpoint], [Builder.endpointRoute],
         * and/or [Builder.workerRoute] is not set.
         *
         * @return The built [TaskRequest] object
         * @throws InvalidCloudTasksRequestException If a property is not set
         */
        fun build(): TaskRequest {
            val exceptionMessage = when {
                endpoint == null -> "No endpoint is given!"
                endpointRoute == null -> "No endpoint route is given!"
                workerRoute == null -> "No worker route is given!"
                else -> null
            }

            if (exceptionMessage != null)
                throw InvalidCloudTasksRequestException(exceptionMessage)

            return TaskRequest(endpoint!!, endpointRoute!!, workerRoute!!)
        }
    }
}
