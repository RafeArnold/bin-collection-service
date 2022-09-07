package uk.co.rafearnold.bincollection

import uk.co.rafearnold.bincollection.model.Command
import java.util.concurrent.CompletableFuture

interface CommandParser {

    /**
     * @throws CommandParserException
     */
    fun parseCommand(command: String): CompletableFuture<Command>

    fun getUsageText(): CompletableFuture<String>
}
