package com.auritylab.spring.gcp.tasks.api.payload

import kotlinx.serialization.Serializable

@Serializable
data class PayloadWrapper<T : Any>(val payload: T)
