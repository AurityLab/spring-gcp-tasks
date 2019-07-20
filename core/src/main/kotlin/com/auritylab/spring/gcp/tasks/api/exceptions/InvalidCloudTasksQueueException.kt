package com.auritylab.spring.gcp.tasks.api.exceptions

class InvalidCloudTasksQueueException(message: String, cause: Throwable? = null) : CloudTasksException(message, cause)
