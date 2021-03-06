package com.auritylab.spring.gcp.tasks.config

import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHandler
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHelper
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    BeanExplorer::class,
    BeanExplorer.Processor::class,
    TaskExecutor::class,
    TaskSignatureHandler::class,
    TaskSignatureHelper::class
)
@EnableConfigurationProperties(CloudTasksProperties::class)
@ConditionalOnBean(CloudTasksLibraryConfiguration.LibraryMarker::class)
class CloudTasksLibraryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(BeanExplorer::class)
    fun beanExplorer() = BeanExplorer()

    @Bean
    @ConditionalOnMissingBean(TaskSignatureHandler::class)
    fun taskSignatureHandler(properties: CloudTasksProperties) = TaskSignatureHandler(properties)

    @Bean
    @ConditionalOnMissingBean(TaskExecutor::class)
    fun taskExecutor(properties: CloudTasksProperties, taskSignatureHelper: TaskSignatureHelper) =
        TaskExecutor(properties, taskSignatureHelper)
}
