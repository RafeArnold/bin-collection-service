package uk.co.rafearnold.bincollection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class AsyncLockManagerImplTest {

    @Test
    fun `operations are synchronised`() {
        val lockManager = AsyncLockManagerImpl()

        var int = 0

        val iterationCount = 10000
        val futures: List<CompletableFuture<Void>> =
            (1..iterationCount).map {
                lockManager.runAsyncWithLock { CompletableFuture.runAsync { int++ } }
            }
        CompletableFuture.allOf(*futures.toTypedArray()).get(10, TimeUnit.SECONDS)
        assertEquals(iterationCount, int)
    }
}
