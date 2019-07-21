package com.auritylab.spring.gcp.tasks.api.utils.request

import org.junit.jupiter.api.Test
import java.net.URL

class TaskRequestTest {

    private fun checkRequestObject(endpoint: String, endpointRoute: String, workerRoute: String, request: TaskRequest)
        = request.buildRequestUrl() == URL(endpoint + endpointRoute) && request.workerRoute == workerRoute

    @Test
    fun `Test TaskRequest object`() {
        val testEndpoint = "http://localhost:3000"
        val testEndpointRoute = "/taskhandler"
        val testWorkerRoute = "/test"

        assert(checkRequestObject(testEndpoint, testEndpointRoute, testWorkerRoute,
            TaskRequest(testEndpoint, testEndpointRoute, testWorkerRoute)))
    }

    @Test
    fun `Test builder of TaskRequest class`() {
        val testEndpoint = "http://localhost:3000"
        val testEndpointRoute = "/taskhandler"
        val testWorkerRoute = "/test"

        val request = TaskRequest {
            setEndpoint(testEndpoint)
            setEndpointRoute(testEndpointRoute)
            setWorkerRoute(testWorkerRoute)
        }

        assert(checkRequestObject(testEndpoint, testEndpointRoute, testWorkerRoute, request))
    }

}
