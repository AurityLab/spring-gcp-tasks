package com.auritylab.spring.gcp.tasks.api.utils.request

import org.junit.jupiter.api.Test
import java.net.URL

class TaskRequestTest {
    private fun checkRequestObject(endpoint: String, endpointRoute: String, workerRoute: String, request: TaskRequest) =
        request.buildRequestUrl() == URL(endpoint + endpointRoute) && request.workerRoute == workerRoute

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

    @Test
    fun `Test builder of TaskQueue class with existing TaskQueue object as base`() {
        val testEndpoint = "http://localhost:3000"
        val testEndpointRoute = "/taskhandler"
        val testWorkerRoute = "/test"

        val testEndpoint2 = "http://localhost:4000"
        val testEndpointRoute2 = "/anotherhandler"
        val testWorkerRoute2 = "/anotherroute"

        val request = TaskRequest {
            setEndpoint(testEndpoint)
            setEndpointRoute(testEndpointRoute)
            setWorkerRoute(testWorkerRoute)
        }

        assert(checkRequestObject(testEndpoint, testEndpointRoute, testWorkerRoute, request))

        assert(checkRequestObject(testEndpoint2, testEndpointRoute, testWorkerRoute, TaskRequest {
            fromTaskRequest(request)
            setEndpoint(testEndpoint2)
        }))

        assert(checkRequestObject(testEndpoint, testEndpointRoute2, testWorkerRoute, TaskRequest {
            fromTaskRequest(request)
            setEndpointRoute(testEndpointRoute2)
        }))

        assert(checkRequestObject(testEndpoint, testEndpointRoute, testWorkerRoute2, TaskRequest {
            fromTaskRequest(request)
            setWorkerRoute(testWorkerRoute2)
        }))
    }
}
