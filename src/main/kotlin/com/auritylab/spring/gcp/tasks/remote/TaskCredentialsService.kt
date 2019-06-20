package com.auritylab.spring.gcp.tasks.remote

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.gax.core.CredentialsProvider
import org.springframework.stereotype.Service

@Service
class TaskCredentialsService(
        private val credentialsProvider: CredentialsProvider
) {
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory = JacksonFactory.getDefaultInstance()
}
