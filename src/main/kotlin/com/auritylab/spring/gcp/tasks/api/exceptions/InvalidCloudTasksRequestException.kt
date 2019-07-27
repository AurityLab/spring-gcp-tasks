package com.auritylab.spring.gcp.tasks.api.exceptions

class InvalidCloudTasksRequestException(message: String, cause: Throwable? = null) : CloudTasksException(message, cause)
