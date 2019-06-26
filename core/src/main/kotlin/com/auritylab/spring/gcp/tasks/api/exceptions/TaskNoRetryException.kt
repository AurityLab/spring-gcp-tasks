package com.auritylab.spring.gcp.tasks.api.exceptions

class TaskNoRetryException(message: String, cause: Throwable? = null) : TaskException(message, cause)
