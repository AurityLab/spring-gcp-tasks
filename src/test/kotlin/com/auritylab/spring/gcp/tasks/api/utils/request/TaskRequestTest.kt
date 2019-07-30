package com.auritylab.spring.gcp.tasks.api.utils.request

import com.auritylab.spring.gcp.tasks.api.exceptions.InvalidCloudTasksRequestException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.MalformedURLException
import java.net.URL

class TaskRequestTest {
    companion object {
        fun checkRequestObject(endpoint: String, endpointRoute: String, workerRoute: String, request: TaskRequest) =
            request.buildRequestUrl() == URL(endpoint + endpointRoute) && request.workerRoute == workerRoute
    }

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
    fun `Test builder of TaskRequest class with existing TaskQueue object as base`() {
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

    @Test
    fun `Test builder of TaskRequest class with missing values`() {
        val testEndpoint = "http://localhost:3000"
        val testEndpointRoute = "/taskhandler"
        val testWorkerRoute = "/test"

        assertThrows<InvalidCloudTasksRequestException> {
            TaskRequest {
                setEndpointRoute(testEndpointRoute)
                setWorkerRoute(testWorkerRoute)
            }
        }
        assertThrows<InvalidCloudTasksRequestException> {
            TaskRequest {
                setEndpoint(testEndpoint)
                setWorkerRoute(testWorkerRoute)
            }
        }
        assertThrows<InvalidCloudTasksRequestException> {
            TaskRequest {
                setEndpointRoute(testEndpointRoute)
                setEndpoint(testEndpoint)
            }
        }
    }

    @Test
    fun `Test TaskRequest#buildRequestUrl with valid endpoint url`() {
        val testEndpoint = "http://localhost:3000"
        val testEndpointRoute = "/taskhandler"
        val testWorkerRoute = "/test"

        val request = TaskRequest(testEndpoint, testEndpointRoute, testWorkerRoute)

        assert(request.buildRequestUrl() == URL(testEndpoint + testEndpointRoute))
    }

    @Test
    fun `Test TaskRequest#buildRequestUrl with invalid endpoint url`() {
        val testEndpoint = "something-invalid"
        val testEndpointRoute = "/taskhandler"
        val testWorkerRoute = "/test"

        val request = TaskRequest(testEndpoint, testEndpointRoute, testWorkerRoute)

        assertThrows<MalformedURLException> {
            request.buildRequestUrl()
        }
    }
}
