package uk.co.rafearnold.bincollection

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.function.Function

class AsyncLockManagerImpl : AsyncLockManager {

    private val semaphore: Semaphore = Semaphore(1)

    private val executor: Executor = Executors.newCachedThreadPool()

    override fun <U> runAsyncWithLock(fn: Function<Void?, CompletionStage<U>>): CompletableFuture<U> =
        CompletableFuture.runAsync({ semaphore.acquire() }, executor)
            .thenCompose(fn)
            .whenComplete { _, _ -> semaphore.release() }
}
