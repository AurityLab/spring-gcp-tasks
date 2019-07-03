package com.auritylab.spring.gcp.tasks.core;

import com.auritylab.spring.gcp.tasks.api.ITaskWorker;
import com.google.cloud.tasks.v2beta3.CloudTasksClient;
import com.google.cloud.tasks.v2beta3.Task;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class TaskExecutorJava {

    public UUID execute(ITaskWorker<?> worker, String payload) throws IOException {
        UUID uuid = UUID.randomUUID();
        String queue = worker.getQueue().toString();
        String base64payload = Base64.getEncoder().encodeToString(payload.getBytes());

        try (CloudTasksClient client = CloudTasksClient.create()) {
            Task.Builder builder = Task.newBuilder();
        }

        return uuid;
    }

}
