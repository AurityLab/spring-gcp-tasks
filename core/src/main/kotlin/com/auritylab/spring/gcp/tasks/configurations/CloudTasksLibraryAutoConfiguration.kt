package com.auritylab.spring.gcp.tasks.configurations

import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import com.auritylab.spring.gcp.tasks.remote.TaskCredentialsService
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(CloudTasksConfiguration::class/*, BeanExplorer::class, TaskCredentialsService::class, TaskExecutor::class*/)
@ConditionalOnBean(CloudTasksLibraryConfiguration.Marker::class)
class CloudTasksLibraryAutoConfiguration {

    init {
        println("Test")
    }

    @Bean
    @ConditionalOnMissingBean(BeanExplorer::class)
    fun beanExplorer() = BeanExplorer()

    @Bean
    @ConditionalOnMissingBean(TaskCredentialsService::class)
    fun taskCredentialsService() = TaskCredentialsService()

    @Bean
    @ConditionalOnMissingBean(TaskExecutor::class)
    fun taskExecutor(taskCredentialsService: TaskCredentialsService) = TaskExecutor(taskCredentialsService)
}
