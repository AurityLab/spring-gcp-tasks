package com.auritylab.spring.gcp.tasks.api.annotations

import org.springframework.stereotype.Service

@Service
annotation class CloudTask(val queue: String)
