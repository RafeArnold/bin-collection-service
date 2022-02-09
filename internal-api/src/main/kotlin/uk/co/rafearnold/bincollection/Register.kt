package uk.co.rafearnold.bincollection

import java.util.concurrent.CompletableFuture

interface Register {

    fun register(): CompletableFuture<Void>
}
