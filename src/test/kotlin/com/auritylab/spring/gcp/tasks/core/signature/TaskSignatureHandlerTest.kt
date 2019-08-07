package com.auritylab.spring.gcp.tasks.core.signature

import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
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
        val uuid = UUID.randomUUID()
        val signature = handler.sign(uuid)

        assert(handler.verify(uuid, signature))
    }

    @Test
    fun `Test signing and verifying of signature with different uuid`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        val signature = handler.sign(uuid1)

        assert(!handler.verify(uuid2, signature))
    }

    @Test
    fun `Test signing and verifying of signature with different signature string`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val uuid = UUID.randomUUID()
        val signature = handler.sign(uuid)

        val modifiedSignature = TaskSignature(signature.signature + "test",
            signature.timestamp, signature.version)

        assert(!handler.verify(uuid, modifiedSignature))
    }

    @Test
    fun `Test signing and verifying of signature with different timestamp`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val uuid = UUID.randomUUID()
        val signature = handler.sign(uuid)

        val modifiedSignature = TaskSignature(signature.signature,
            signature.timestamp - 10000, signature.version)

        assert(!handler.verify(uuid, modifiedSignature))
    }

    @Test
    fun `Test signing and verifying of signature with different version`(
        @Autowired handler: TaskSignatureHandler
    ) {
        val uuid = UUID.randomUUID()
        val signature = handler.sign(uuid)

        val modifiedSignature = TaskSignature(signature.signature,
            signature.timestamp, signature.version - 5)

        assert(!handler.verify(uuid, modifiedSignature))
    }
}
