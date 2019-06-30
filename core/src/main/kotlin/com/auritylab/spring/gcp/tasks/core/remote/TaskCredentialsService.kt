package com.auritylab.spring.gcp.tasks.core.remote

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.cloudtasks.v2beta3.CloudTasks
import com.google.auth.oauth2.GoogleCredentials
import org.springframework.stereotype.Service

@Service
class TaskCredentialsService {
    private lateinit var cloudTasks: CloudTasks

    init {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()

        var credentials = GoogleCredentials.getApplicationDefault()
        if (credentials.createScopedRequired())
            credentials = credentials.createScoped(arrayListOf("https://www.googleapis.com/auth/cloud-platform"))

        cloudTasks = CloudTasks.Builder(httpTransport, jsonFactory, HttpRequestInitializer {
            request -> request!!.headers.authorization = credentials.accessToken.tokenValue
        }).build()
    }

    fun getCloudTasks(): CloudTasks = cloudTasks
}
