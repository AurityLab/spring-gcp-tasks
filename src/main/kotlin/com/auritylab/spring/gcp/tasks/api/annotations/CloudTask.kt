package com.auritylab.spring.gcp.tasks.api.annotations

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class CloudTask(
    /** The GCP project id to use for this worker. Defaults to global configuration. */
    val projectId: String = "$",

    /** The GCP location id to use for this worker. Defaults to global configuration. */
    val locationId: String = "$",

    /** The GCP queue id to use for this worker. Defaults to global configuration. */
    val queueId: String = "$",

    /** The endpoint to use for this worker. Defaults to global configuration. */
    val endpoint: String = ":",

    /** The endpoint route to use for this worker. Defaults to global configuration. */
    val endpointRoute: String = ":",

    /** The worker route to use for this worker. Defaults to global configuration. */
    val route: String = ":"
)
