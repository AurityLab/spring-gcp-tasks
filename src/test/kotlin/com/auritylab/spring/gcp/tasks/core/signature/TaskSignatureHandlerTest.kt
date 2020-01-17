package com.auritylab.spring.gcp.tasks.core.signature

import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
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
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [TaskSignatureHandlerTest.TestConfiguration::class])
@TestPropertySource("/test-base.properties")
class TaskSignatureHandlerTest {
    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project-by-provider" }
    }

    @Test
    fun `Test signing and verifying signature`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val data = createTestSignatureData()

        assert(signAndVerify(handler, data, data))
    }

    @Test
    fun `Test signing and verifying of signature with different signature string`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val data = createTestSignatureData()
        val signature = handler.sign(data)

        val modifiedSignature = TaskSignature(data, signature.signature + "test")

        assert(!handler.verify(modifiedSignature))
    }

    @Test
    fun `Test signing and verifying of signature with different payload`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val data1 = createTestSignatureData(payload = "some-payload-1")
        val data2 = createTestSignatureData(payload = "some-payload-2")

        assert(!signAndVerify(handler, data1, data2))
    }

    @Test
    fun `Test signing and verifying of signature with different worker route`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val data1 = createTestSignatureData(workerRoute = "/testWorker-1")
        val data2 = createTestSignatureData(workerRoute = "/testWorker-2")

        assert(!signAndVerify(handler, data1, data2))
    }

    @Test
    fun `Test signing and verifying of signature with different task id`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val data1 = createTestSignatureData(taskId = UUID.randomUUID().toString())
        val data2 = createTestSignatureData(taskId = UUID.randomUUID().toString())

        assert(!signAndVerify(handler, data1, data2))
    }

    @Test
    fun `Test signing and verifying of signature with different user agent`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val data1 = createTestSignatureData(userAgent = "some-agent-1")
        val data2 = createTestSignatureData(userAgent = "some-agent-2")

        assert(!signAndVerify(handler, data1, data2))
    }

    @Test
    fun `Test signing and verifying of signature with different timestamp`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val timestamp1 = testTimestamp
        val timestamp2 = (testTimestamp.toLong() - 1000).toString()

        val data1 = createTestSignatureData(timestamp = timestamp1)
        val data2 = createTestSignatureData(timestamp = timestamp2)

        assert(!signAndVerify(handler, data1, data2))
    }

    @Test
    fun `Test signing and verifying of signature with different version`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val data1 = createTestSignatureData(version = "4")
        val data2 = createTestSignatureData(version = "5")

        assert(!signAndVerify(handler, data1, data2))
    }

    @Test
    fun `Test with different secrets for signing and verifying`() {
        val properties1 = CloudTasksProperties().apply {
            signatureSecret = "n2TcmoPkKT4NdqQBGcxv4nsKGP8N014V"
        }
        val properties2 = CloudTasksProperties().apply {
            signatureSecret = "pQQepqHsVmFDeiKGf7avvwaZmKClPayO"
        }

        val handler1 = TaskSignatureHandler(properties1)
        val handler2 = TaskSignatureHandler(properties2)

        val data = createTestSignatureData()
        val signature = handler1.sign(data)

        assert(!handler2.verify(signature))
    }

    private val testUuid = UUID.randomUUID().toString()
    private val testTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC).toString()

    private fun createTestSignatureData(
        payload: String? = null,
        workerRoute: String? = null,
        taskId: String? = null,
        userAgent: String? = null,
        timestamp: String? = null,
        version: String? = null
    ) = TaskSignatureData(
        payload = payload ?: "some-payload", workerRoute = workerRoute ?: "/testWorker",
        taskId = taskId ?: testUuid, userAgent = userAgent ?: TaskExecutor.USER_AGENT_HEADER_VALUE,
        timestamp = timestamp ?: testTimestamp, version = version ?: TaskSignatureHelper.CURRENT_VERSION.toString()
    )

    private fun signAndVerify(
        handler: TaskSignatureHandler,
        toBeSigned: TaskSignatureData,
        toBeVerified: TaskSignatureData
    ): Boolean {
        val actualSignature = handler.sign(toBeSigned)
        val createdSignature = TaskSignature(toBeVerified, actualSignature.signature)

        return handler.verify(createdSignature)
    }
}
