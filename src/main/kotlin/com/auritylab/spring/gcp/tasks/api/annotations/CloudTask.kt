package com.auritylab.spring.gcp.tasks.api.annotations

import org.springframework.stereotype.Service

@Service
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class CloudTask(val queue: String)
