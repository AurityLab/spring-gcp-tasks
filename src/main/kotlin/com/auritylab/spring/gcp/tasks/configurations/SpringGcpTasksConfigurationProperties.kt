package com.auritylab.spring.gcp.tasks.configurations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.annotation.Nullable
import javax.annotation.Nonnull

@Component
@ConfigurationProperties("com.auritylab.spring.gcp.tasks")
@Validated
class SpringGcpTasksConfigurationProperties {
    @Nonnull
    lateinit var workerEndpoint: String

    @Nullable
    var defaultProjectId: String? = null

    @Nullable
    var defaultLocationId: String? = null

    @Nullable
    var defaultQueueId: String? = null
}
