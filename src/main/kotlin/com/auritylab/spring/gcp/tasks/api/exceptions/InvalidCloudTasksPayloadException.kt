package com.auritylab.spring.gcp.tasks.api.exceptions

class InvalidCloudTasksPayloadException(message: String, cause: Throwable? = null) : CloudTasksException(message, cause)
