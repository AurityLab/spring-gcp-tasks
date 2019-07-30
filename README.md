# Spring GCP Cloud Tasks library

Spring-gcp-tasks is a small library to integrate GCP Cloud Tasks into Spring projects.

---

* Installation
* Properties
* Example

---

### Installation

Gradle:
```groovy
dependencies {
    implementation 'com.auritylab.spring.gcp.tasks:core:0.1.4-SNAPSHOT'
}
```

Maven:
```xml
<dependency>
  <groupId>com.auritylab.spring.gcp.tasks</groupId>
  <artifactId>core</artifactId>
  <version>0.1.4-SNAPSHOT</version>
</dependency>
```

Also you **have** to use google-cloud-bom version `0.99.0-alpha` or higher:

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

### Properties

*TODO*

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
