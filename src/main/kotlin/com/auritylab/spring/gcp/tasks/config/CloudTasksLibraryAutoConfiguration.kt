package com.auritylab.spring.gcp.tasks.config

import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.TaskEndpoint
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import com.auritylab.spring.gcp.tasks.core.signature.TaskSignatureHandler
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
    TaskEndpoint::class,
    TaskSignatureHandler::class
)
@EnableConfigurationProperties(CloudTasksProperties::class)
@ConditionalOnBean(CloudTasksLibraryConfiguration.Marker::class)
class CloudTasksLibraryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(BeanExplorer::class)
    fun beanExplorer() = BeanExplorer()

    @Bean
    @ConditionalOnMissingBean(TaskExecutor::class)
    fun taskExecutor(properties: CloudTasksProperties) = TaskExecutor(properties)
}
