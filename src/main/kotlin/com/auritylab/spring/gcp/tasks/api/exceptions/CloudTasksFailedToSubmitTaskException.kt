package com.auritylab.spring.gcp.tasks.api.exceptions

class CloudTasksFailedToSubmitTaskException(message: String, cause: Throwable? = null) : CloudTasksException(message, cause)
