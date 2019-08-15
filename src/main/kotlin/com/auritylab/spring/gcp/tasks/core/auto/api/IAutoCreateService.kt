package com.auritylab.spring.gcp.tasks.core.auto.api

interface IAutoCreateService<T : Any> {
    fun handle(obj: T)
}
