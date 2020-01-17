package com.auritylab.spring.gcp.tasks.core.signature

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class TaskSignatureHelper(private val handler: TaskSignatureHandler) {
    companion object {
        const val CURRENT_VERSION: Int = 1
    }

    fun createNewSignatureData(
        payload: String, route: String, id: UUID, userAgent: String
    ) = TaskSignatureData(
        payload, route, id.toString(), userAgent,
        currentTimestamp().toString(), CURRENT_VERSION.toString()
    )

    fun createFromRequestSignatureData(
        payload: String, cloudTasksRouteHeader: String,
        cloudTasksIdHeader: String, userAgentHeader: String,
        cloudTasksTimestampHeader: String, cloudTasksVersionHeader: String
    ) = TaskSignatureData(
        payload, cloudTasksRouteHeader, cloudTasksIdHeader,
        userAgentHeader, cloudTasksTimestampHeader, cloudTasksVersionHeader
    )

    fun getHandler() = handler

    private fun currentTimestamp(): Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
}
