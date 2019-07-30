package com.auritylab.spring.gcp.tasks.api

import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.api.utils.queue.TaskQueueTest
import com.auritylab.spring.gcp.tasks.api.utils.request.TaskRequestTest
import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [TaskWorkerSettingsTest.TestConfiguration::class])
@TestPropertySource("/test-base.properties")
class TaskWorkerSettingsTest {
    @CloudTask
    class TestWorker1 : TaskWorker<TestWorker1.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {}
        data class Payload(val str: String)
    }

    @CloudTask(
        projectId = "custom-test-project-id", locationId = "custom-test-location-id",
        queueId = "custom-test-queue-id",
        endpoint = "http://127.0.0.1:5000", endpointRoute = "/custom-test-endpoint-route",
        route = "custom-test-worker-route"
    )
    class TestWorker2 : TaskWorker<TestWorker2.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {}
        data class Payload(val str: String)
    }

    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean fun testWorker1(): TestWorker1 = TestWorker1()
        @Bean fun testWorker2(): TestWorker2 = TestWorker2()

        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    private fun <T : TaskWorker<*>> getCloudTaskAnnotation(clazz: KClass<T>): CloudTask? = clazz.findAnnotation()

    @Test
    fun `Test ITaskWorkerSettings with defaults in properties file`(
        @Autowired properties: CloudTasksProperties,
        @Autowired gcpProjectIdProvider: GcpProjectIdProvider
    ) {
        val annotation = getCloudTaskAnnotation(TestWorker1::class)
        val settings = TaskWorkerSettings(properties, gcpProjectIdProvider, annotation)

        assert(TaskQueueTest.checkQueueObject(properties.defaultProjectId!!, properties.defaultLocationId!!,
            properties.defaultQueueId!!, settings.taskQueue))

        assert(TaskRequestTest.checkRequestObject(properties.defaultWorkerEndpoint!!,
            properties.defaultWorkerEndpointRoute, properties.defaultWorkerRoute, settings.taskRequest))
    }

    @Test
    fun `Test ITaskWorkerSettings with project id from GcpProjectIdProvider`(
        @Autowired properties: CloudTasksProperties,
        @Autowired gcpProjectIdProvider: GcpProjectIdProvider
    ) {
        val modifiedProperties = CloudTasksProperties().apply {
            // Skipping defaultProjectId here
            defaultLocationId = properties.defaultLocationId
            defaultQueueId = properties.defaultQueueId
            defaultWorkerEndpoint = properties.defaultWorkerEndpoint
            defaultWorkerEndpointRoute = properties.defaultWorkerEndpointRoute
            defaultWorkerRoute = properties.defaultWorkerRoute
            skipCloudTasks = properties.skipCloudTasks
            skipTaskEndpoint = properties.skipTaskEndpoint
        }

        val annotation = getCloudTaskAnnotation(TestWorker1::class)
        val settings = TaskWorkerSettings(modifiedProperties, gcpProjectIdProvider, annotation)

        assert(TaskQueueTest.checkQueueObject(gcpProjectIdProvider.projectId, modifiedProperties.defaultLocationId!!,
            modifiedProperties.defaultQueueId!!, settings.taskQueue))

        assert(TaskRequestTest.checkRequestObject(modifiedProperties.defaultWorkerEndpoint!!,
            modifiedProperties.defaultWorkerEndpointRoute, modifiedProperties.defaultWorkerRoute, settings.taskRequest))
    }

    @Test
    fun `Test ITaskWorkerSettings with default in CloudTask annotation`(
        @Autowired properties: CloudTasksProperties,
        @Autowired gcpProjectIdProvider: GcpProjectIdProvider
    ) {
        val annotation = getCloudTaskAnnotation(TestWorker2::class)
        val settings = TaskWorkerSettings(properties, gcpProjectIdProvider, annotation)

        assert(TaskQueueTest.checkQueueObject("custom-test-project-id", "custom-test-location-id",
            "custom-test-queue-id", settings.taskQueue))

        assert(TaskRequestTest.checkRequestObject("http://127.0.0.1:5000",
            "/custom-test-endpoint-route", "custom-test-worker-route", settings.taskRequest))
    }

    @Test
    fun `Test ITaskWorkerSettings queue object updating`(
        @Autowired properties: CloudTasksProperties,
        @Autowired gcpProjectIdProvider: GcpProjectIdProvider
    ) {
        val annotation = getCloudTaskAnnotation(TestWorker2::class)
        val settings = TaskWorkerSettings(properties, gcpProjectIdProvider, annotation)

        settings.updateTaskQueue {
            setProjectId("updated-project-id")
        }
        assert(TaskQueueTest.checkQueueObject("updated-project-id", "custom-test-location-id",
            "custom-test-queue-id", settings.taskQueue))

        settings.updateTaskQueue {
            setLocationId("updated-location-id")
        }
        assert(TaskQueueTest.checkQueueObject("updated-project-id", "updated-location-id",
            "custom-test-queue-id", settings.taskQueue))

        settings.updateTaskQueue {
            setQueueId("updated-queue-id")
        }
        assert(TaskQueueTest.checkQueueObject("updated-project-id", "updated-location-id",
            "updated-queue-id", settings.taskQueue))
    }

    @Test
    fun `Test ITaskWorkerSettings request object updating`(
        @Autowired properties: CloudTasksProperties,
        @Autowired gcpProjectIdProvider: GcpProjectIdProvider
    ) {
        val annotation = getCloudTaskAnnotation(TestWorker2::class)
        val settings = TaskWorkerSettings(properties, gcpProjectIdProvider, annotation)

        settings.updateTaskRequest {
            setEndpoint("http://127.0.0.1:7000")
        }
        assert(TaskRequestTest.checkRequestObject("http://127.0.0.1:7000",
            "/custom-test-endpoint-route", "custom-test-worker-route", settings.taskRequest))

        settings.updateTaskRequest {
            setEndpointRoute("/updated-endpoint-route")
        }
        assert(TaskRequestTest.checkRequestObject("http://127.0.0.1:7000",
            "/updated-endpoint-route", "custom-test-worker-route", settings.taskRequest))

        settings.updateTaskRequest {
            setWorkerRoute("updated-worker-route")
        }
        assert(TaskRequestTest.checkRequestObject("http://127.0.0.1:7000",
            "/updated-endpoint-route", "updated-worker-route", settings.taskRequest))
    }
}
