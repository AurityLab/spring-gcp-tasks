package com.auritylab.spring.gcp.tasks.configurations

import com.auritylab.spring.gcp.tasks.core.BeanExplorer
import com.auritylab.spring.gcp.tasks.core.TaskExecutor
import com.auritylab.spring.gcp.tasks.remote.TaskCredentialsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(CloudTasksConfiguration::class, BeanExplorer::class, TaskCredentialsService::class, TaskExecutor::class)
@ConditionalOnBean(CloudTasksLibraryConfiguration.Marker::class)
class CloudTasksLibraryAutoConfiguration {
    /*@Autowired
    lateinit var beanExplorer: BeanExplorer

    @Autowired
    lateinit var taskExecutor: TaskExecutor

    @Autowired
    lateinit var taskCredentialsService: TaskCredentialsService*/
}
