package com.auritylab.spring.gcp.tasks.api.exceptions

open class TaskException(message: String?, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String) : this(message, null)
    constructor(cause: Throwable) : this(null, cause)
    constructor() : this(null, null)
}
