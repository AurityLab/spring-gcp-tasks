# Spring GCP Cloud Tasks library

Spring-gcp-tasks is a small library to integrate GCP Cloud Tasks into Spring projects.

Note: *This isn't heavily production tested yet, but the library is in a finished state.*

---

* [Installation](https://github.com/AurityLab/spring-gcp-tasks#installation)
* [Compatibility](https://github.com/AurityLab/spring-gcp-tasks#compatibility)
* [Properties](https://github.com/AurityLab/spring-gcp-tasks#properties)
* [Usage](https://github.com/AurityLab/spring-gcp-tasks#usage)
* [Example](https://github.com/AurityLab/spring-gcp-tasks#example)

---

### Installation

Gradle:
```groovy
dependencies {
    implementation "com.auritylab.spring-gcp-tasks:0.1.11"
}
```

Maven:
```xml
<dependency>
  <groupId>com.auritylab</groupId>
  <artifactId>spring-gcp-tasks</artifactId>
  <version>0.1.11</version>
</dependency>
```

### Compatibility

This library is compatible with spring cloud `Hoxton` or higher (at least `Hoxton.M2`).

For everything below `Hoxton.M2` (`Greenwich` etc.) you **have** to use google-cloud-bom version `0.99.0-alpha` or higher:

Gradle:
```groovy
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}") {
            bomProperty "google-cloud-bom.version", "0.99.0-alpha"
        }
    }
}
```

Maven:
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-bom</artifactId>
    <version>0.99.0-alpha</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Replace `${springCloudVersion}` with your version (e.g. `Greenwich.SR2`).

### Properties

Prefix for all properties: `com.auritylab.spring.gcp.tasks`

Property | Default | Required | Type | Description
------------ | ------------- | ------------- | ------------- | -------------
`signature-secret` | **nothing** | **yes** | `String` | Used for signing tasks before submitting to Cloud Tasks.
`default-project-id` | `null` | **no** | `String?` | Used when nothing specified in `@CloudTask` annotation.
`default-location-id` | `null` | **no** | `String?` | Used when nothing specified in `@CloudTask` annotation.
`default-queue-id` | `null` | **no** | `String?` | Used when nothing specified in `@CloudTask` annotation.
`default-worker-endpoint` | `null` | **no** | `String?` | Used when nothing specified in `@CloudTask` annotation.
`default-worker-endpoint-route` | `"/taskhandler"` | **default** | `String` | Used when nothing specified in `@CloudTask` annotation.
`default-worker-route` | `""` | **default** | `String` | Used when nothing specified in `@CloudTask` annotation.
`skip-cloud-tasks` | `false` | **default** | `Boolean` | Task submissions will be sent to the task's worker endpoint directly skipping Cloud Tasks.
`skip-task-endpoint` | `false` | **default** | `Boolean` | Task will be executed directly without networking (task's execute method blocks until task finished).
`queue-id-global-prefix` | `""` | **default** | `String` | Prefix used for all queue ids. Useful for testing or staging.

Note on types:<br>`String` -> non-nullable<br>`String?` -> nullable

Also note that `default-worker-endpoint-route` and `default-worker-route` are two different properties.
The former is used for the actual route to send the task to (in combination with `default-worker-endpoint`).
The latter is a *virtual* route to map the task to the right worker.

### Usage

There are two annotations for auto configuration:<br>
`@EnableCloudTasks` and `@EnableCloudTasksWithEndpoint`

The former enabled spring-gcp-tasks functionality, but disables the worker endpoint.

The latter also enables the worker endpoint via a rest controller mapped to the route specified
in `com.auritylab.spring.gcp.tasks.default-worker-endpoint-route`. A configured web server is needed for this.

```kotlin
@SpringBootApplication
// or @EnableCloudTasks to disable worker endpoint
@EnableCloudTasksWithEndpoint
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

### Example

Writing a task worker:

```kotlin
@CloudTask(route = "/notification")
class NotificationWorker : TaskWorker<NotificationWorker.Payload>(Payload::class) {
    override fun run(payload: Payload, id: UUID) {
        println("New notification task: $payload")
    }

    data class Payload(val str: String)
}
```

Executing a task with worker above:

```kotlin
class NotificationTest(
    private val notificationWorker: NotificationWorker
) {
    fun execute() {
        notificationWorker.execute(
            NotificationWorker.Payload("test")
        )
    }
}
```
