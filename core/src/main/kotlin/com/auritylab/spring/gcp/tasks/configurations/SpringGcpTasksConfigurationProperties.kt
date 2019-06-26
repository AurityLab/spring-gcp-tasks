package com.auritylab.spring.gcp.tasks.configurations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.annotation.Nonnull
import javax.annotation.Nullable

@Component
@ConfigurationProperties("com.auritylab.spring.gcp.tasks")
@Validated
class SpringGcpTasksConfigurationProperties {
    @Nullable
    var defaultProjectId: String? = null

    @Nullable
    var defaultLocationId: String? = null

    @Nullable
    var defaultQueueId: String? = null

    @Nullable
    var defaultWorkerEndpoint: String? = null

    var defaultWorkerSubRoute: String = ""

    var workerMainRoute: String = "/tasks"
}
