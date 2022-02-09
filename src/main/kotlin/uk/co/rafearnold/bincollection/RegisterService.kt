package uk.co.rafearnold.bincollection

import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class RegisterService @Inject constructor(
    private val registers: Set<@JvmSuppressWildcards Register>
) : Register {

    override fun register(): CompletableFuture<Void> =
        CompletableFuture.allOf(*registers.map { it.register() }.toTypedArray())
}
