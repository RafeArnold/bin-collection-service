package uk.co.rafearnold.bincollection.messengerbot

import com.restfb.DefaultFacebookClient
import com.restfb.FacebookClient
import com.restfb.Version

class MessengerApiClientFactory {
    fun createClient(accessToken: String, appSecret: String): FacebookClient =
        DefaultFacebookClient(accessToken, appSecret, Version.LATEST)
}
