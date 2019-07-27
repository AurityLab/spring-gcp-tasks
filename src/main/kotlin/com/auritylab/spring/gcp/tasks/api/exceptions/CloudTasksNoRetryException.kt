package com.auritylab.spring.gcp.tasks.api.exceptions

class CloudTasksNoRetryException(message: String, cause: Throwable? = null) : CloudTasksException(message, cause)
