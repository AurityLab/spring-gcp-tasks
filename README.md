# Spring GCP Cloud Tasks library

Compiler argument needed: `-Xuse-experimental=kotlin.Experimental`

```kotlin
@CloudTask(customRoute = "/notification")
class NotificationWorker : ITaskWorker<NotificationWorker.Payload>(Payload::class) {
    override fun run(payload: Payload, id: UUID) {
        println("New notification task: $payload")
    }

    @Serializable
    data class Payload(val str: String)
}

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
