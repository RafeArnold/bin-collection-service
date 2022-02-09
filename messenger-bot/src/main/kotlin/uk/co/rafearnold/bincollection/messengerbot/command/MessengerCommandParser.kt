package uk.co.rafearnold.bincollection.messengerbot.command

import java.util.concurrent.CompletableFuture

interface MessengerCommandParser {

    /**
     * @throws MessengerCommandParserException
     */
    fun parseCommand(command: String): CompletableFuture<MessengerCommand>
}
