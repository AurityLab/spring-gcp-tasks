package com.auritylab.spring.gcp.tasks.api.exceptions

class TaskFailedToSubmitException(message: String, cause: Throwable? = null) : TaskException(message, cause)
