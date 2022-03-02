package uk.co.rafearnold.bincollection

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Function

interface AsyncLockManager {

    fun <U> runAsyncWithLock(fn: Function<Void?, CompletionStage<U>>): CompletableFuture<U>
}
