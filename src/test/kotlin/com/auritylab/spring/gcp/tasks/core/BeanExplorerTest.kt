package com.auritylab.spring.gcp.tasks.core

import com.auritylab.spring.gcp.tasks.api.ITaskWorker
import com.auritylab.spring.gcp.tasks.api.annotations.CloudTask
import com.auritylab.spring.gcp.tasks.config.CloudTasksLibraryAutoConfiguration
import com.auritylab.spring.gcp.tasks.config.EnableCloudTasks
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gcp.core.GcpProjectIdProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@EnableCloudTasks
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BeanExplorerTest.TestConfiguration::class])
@TestPropertySource("/test-base.properties")
class BeanExplorerTest {
    @CloudTask(route = "test-worker-1")
    class TestWorker1 : ITaskWorker<TestWorker1.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {}
        data class Payload(val str: String)
    }

    @CloudTask(route = "test-worker-2")
    class TestWorker2 : ITaskWorker<TestWorker2.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {}
        data class Payload(val str: String)
    }

    @CloudTask(route = "test-worker-3")
    class TestWorker3 : ITaskWorker<TestWorker3.Payload>(Payload::class) {
        override fun run(payload: Payload, id: UUID) {}
        data class Payload(val str: String)
    }

    @Component
    class SomeIrrelevantBean

    @Configuration
    class TestConfiguration : CloudTasksLibraryAutoConfiguration() {
        @Bean fun testWorker1(): TestWorker1 = TestWorker1()
        @Bean fun testWorker2(): TestWorker2 = TestWorker2()
        @Bean fun testWorker3(): TestWorker3 = TestWorker3()
        @Bean fun someIrrelevantBean(): SomeIrrelevantBean = SomeIrrelevantBean()

        @Bean
        fun gcpProjectIdProvider(): GcpProjectIdProvider = GcpProjectIdProvider { "some-project" }
    }

    @Test
    fun `Test BeanExplorer by route`(
        @Autowired explorer: BeanExplorer
    ) {
        assert(explorer.getWorkerByRoute("test-worker-1") is TestWorker1)
        assert(explorer.getWorkerByRoute("test-worker-2") is TestWorker2)
        assert(explorer.getWorkerByRoute("test-worker-3") is TestWorker3)

        assert(explorer.getWorkerByRoute("test-worker-1") !is TestWorker2 &&
            explorer.getWorkerByRoute("test-worker-1") !is TestWorker3)

        assert(explorer.getWorkerByRoute("test-worker-2") !is TestWorker1 &&
            explorer.getWorkerByRoute("test-worker-2") !is TestWorker3)

        assert(explorer.getWorkerByRoute("test-worker-3") !is TestWorker1 &&
            explorer.getWorkerByRoute("test-worker-3") !is TestWorker2)
    }

    @Test
    fun `Test BeanExplorer by list`(
        @Autowired explorer: BeanExplorer
    ) {
        val list = explorer.getWorkers()

        var testWorker1: TestWorker1? = null
        var testWorker2: TestWorker2? = null
        var testWorker3: TestWorker3? = null
        var someIrrelevantBean: SomeIrrelevantBean? = null

        list.forEach {
            if (it is TestWorker1) testWorker1 = it
            if (it is TestWorker2) testWorker2 = it
            if (it is TestWorker3) testWorker3 = it
            if (it is SomeIrrelevantBean) someIrrelevantBean = it
        }

        assert(testWorker1 != null)
        assert(testWorker2 != null)
        assert(testWorker3 != null)
        assert(someIrrelevantBean == null)
    }

    @Test
    fun `Test BeanExplorer#getWorkers modifiability`(
        @Autowired explorer: BeanExplorer
    ) {
        val list = explorer.getWorkers() as MutableList
        assertThrows<UnsupportedOperationException> {
            list.removeAt(0)
        }
    }
}
