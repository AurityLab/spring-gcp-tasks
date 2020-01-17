package com.auritylab.spring.gcp.tasks.core.signature

import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import com.google.common.hash.Hashing
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Base64

data class TaskSignatureData(val payload: String, val workerRoute: String,
    val taskId: String, val userAgent: String,
    val timestamp: String, val version: String
)

data class TaskSignature(val data: TaskSignatureData, val signature: String)

@Service
class TaskSignatureHandler(
    private val properties: CloudTasksProperties
) {
    fun sign(data: TaskSignatureData): TaskSignature {
        val signature = createSignature(properties.signatureSecret, data)
        val encodedSignature = encodeSignature(signature)

        return TaskSignature(data, encodedSignature)
    }

    fun verify(signature: TaskSignature): Boolean {
        val taskData = signature.data

        val createdSignature = createSignature(properties.signatureSecret, taskData)
        val givenSignature = decodeSignature(signature.signature)

        return createdSignature.contentEquals(givenSignature)
    }

    private fun createSignature(secret: String, data: TaskSignatureData): ByteArray {
        val hash = Hashing.hmacSha256(secret.toByteArray())
            .newHasher()
            .putString(data.payload, StandardCharsets.UTF_16)
            .putString(data.workerRoute, StandardCharsets.UTF_8)
            .putString(data.taskId, StandardCharsets.UTF_8)
            .putString(data.userAgent, StandardCharsets.UTF_8)
            .putString(data.timestamp, StandardCharsets.UTF_8)
            .putString(data.version, StandardCharsets.UTF_8)
            .hash()
        return hash.asBytes()
    }

    private fun encodeSignature(signature: ByteArray): String =
        Base64.getEncoder().withoutPadding().encodeToString(signature)

    private fun decodeSignature(signature: String): ByteArray =
        Base64.getDecoder().decode(signature)
}
