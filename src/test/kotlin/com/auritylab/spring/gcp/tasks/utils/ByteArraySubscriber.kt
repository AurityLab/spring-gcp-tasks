package com.auritylab.spring.gcp.tasks.utils

import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.util.concurrent.Flow

class ByteArraySubscriber(
    private val wrapped: HttpResponse.BodySubscriber<ByteArray>
) : Flow.Subscriber<ByteBuffer> {
    override fun onComplete() {
        wrapped.onComplete()
    }

    override fun onSubscribe(subscription: Flow.Subscription?) {
        wrapped.onSubscribe(subscription)
    }

    override fun onNext(item: ByteBuffer?) {
        wrapped.onNext(listOf(item))
    }

    override fun onError(throwable: Throwable?) {
        wrapped.onError(throwable)
    }
}
