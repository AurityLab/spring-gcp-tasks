package com.auritylab.spring.gcp.tasks.api.annotations

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class CloudSchedule(
    /** The cron value for this scheduled job. */
    val cron: String = "$"
)
