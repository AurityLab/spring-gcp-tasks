package com.auritylab.spring.gcp.tasks.config

import com.auritylab.spring.gcp.tasks.core.config.CloudTasksConfiguration
import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import com.auritylab.spring.gcp.tasks.core.remote.TaskCredentialsService
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(CloudTasksConfiguration::class, BeanExplorer::class, TaskCredentialsService::class, TaskExecutor::class)
@ConditionalOnBean(CloudTasksLibraryConfiguration.Marker::class)
class CloudTasksLibraryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(BeanExplorer::class)
    fun beanExplorer() = BeanExplorer()

    @Bean
    @ConditionalOnMissingBean(TaskCredentialsService::class)
    fun taskCredentialsService() = TaskCredentialsService()

    @Bean
    @ConditionalOnMissingBean(TaskExecutor::class)
    fun taskExecutor() = TaskExecutor(taskCredentialsService())
}