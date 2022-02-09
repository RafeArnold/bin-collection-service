package uk.co.rafearnold.bincollection.messengerbot

import com.restfb.FacebookClient
import com.restfb.Parameter
import com.restfb.types.send.IdMessageRecipient
import com.restfb.types.send.Message
import com.restfb.types.send.SendResponse
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class MessengerMessageInterfaceImpl @Inject constructor(
    apiClientFactory: MessengerApiClientFactory,
    appProps: Map<String, String>
) : MessengerMessageInterface {

    private val apiClient: FacebookClient =
        apiClientFactory.createClient(
            accessToken = appProps.getValue("messenger-bot.access-token"),
            appSecret = appProps.getValue("messenger-bot.app-secret")
        )

    override fun sendMessage(userId: String, messageText: String): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            apiClient.publish(
                "me/messages", SendResponse::class.java,
                Parameter.with("recipient", IdMessageRecipient(userId)),
                Parameter.with("message", Message(messageText))
            )
        }
}
