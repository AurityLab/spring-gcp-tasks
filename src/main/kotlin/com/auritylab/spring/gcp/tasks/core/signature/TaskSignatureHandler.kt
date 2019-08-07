package com.auritylab.spring.gcp.tasks.core.signature

import com.auritylab.spring.gcp.tasks.properties.CloudTasksProperties
import com.google.common.hash.Hashing
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64
import java.util.UUID

@Service
class TaskSignatureHandler(
    private val properties: CloudTasksProperties
) {
    companion object {
        private const val CURRENT_VERSION: Int = 1
    }

    fun sign(uuid: UUID): TaskSignature {
        val timestamp = currentTimestamp()
        val version = CURRENT_VERSION

        val payload = encodePayload(uuid, timestamp, version)
        val secret = properties.signatureSecret.toByteArray()

        val signature = createSignature(payload, secret)
        val encodedSignature = encodeSignature(signature)

        return TaskSignature(encodedSignature, timestamp, version)
    }

    fun verify(uuid: UUID, taskSignature: TaskSignature): Boolean {
        val timestamp = taskSignature.timestamp
        val version = taskSignature.version

        val payload = encodePayload(uuid, timestamp, version)
        val secret = properties.signatureSecret.toByteArray()

        val createdSignature = createSignature(payload, secret)
        val givenSignature = decodeSignature(taskSignature.signature)

        return createdSignature.contentEquals(givenSignature)
    }

    private fun encodeSignature(signature: ByteArray): String =
        Base64.getEncoder().withoutPadding().encodeToString(signature)

    private fun decodeSignature(signature: String): ByteArray =
        Base64.getDecoder().decode(signature)

    private fun createSignature(payload: ByteArray, secret: ByteArray): ByteArray {
        val hash = Hashing.hmacSha256(secret)
            .newHasher()
            .putBytes(payload)
            .hash()

        return hash.asBytes()
    }

    private fun encodePayload(uuid: UUID, timestamp: Long, version: Int): ByteArray {
        val byteBuffer = ByteBuffer.allocate(28).apply {
            putLong(uuid.mostSignificantBits)
            putLong(uuid.leastSignificantBits)
            putLong(timestamp)
            putInt(version)
        }

        return byteBufferToByteArray(byteBuffer)
    }

    private fun byteBufferToByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        return ByteArray(buffer.remaining()).apply {
            buffer.get(this)
        }
    }

    private fun currentTimestamp(): Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
}
