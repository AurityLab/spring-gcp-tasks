package com.auritylab.spring.gcp.tasks.remote

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.gax.core.CredentialsProvider
import com.google.api.services.cloudtasks.v2.CloudTasks
import com.google.api.services.cloudtasks.v2.CloudTasksRequestInitializer
import org.springframework.stereotype.Service

@Service
class TaskCredentialsService(
        private val credentialsProvider: CredentialsProvider
) {
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val jsonFactory = JacksonFactory.getDefaultInstance()

    private val credentials = credentialsProvider.credentials

    val cloudTasks = CloudTasks.Builder(
            httpTransport,
            jsonFactory,
            HttpRequestInitializer {})
}
