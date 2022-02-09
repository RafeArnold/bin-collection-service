package uk.co.rafearnold.bincollection

import java.util.concurrent.CompletableFuture

fun interface Handler<T> {
    fun handle(event: T): CompletableFuture<Void>
}
