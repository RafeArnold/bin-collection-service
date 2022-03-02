package uk.co.rafearnold.bincollection.messengerbot.repository

import com.google.inject.AbstractModule
import com.google.inject.Scopes

class MessengerBotRepositoryModule : AbstractModule() {

    override fun configure() {
        bind(UserInfoRepository::class.java).to(RedisUserInfoRepository::class.java).`in`(Scopes.SINGLETON)
    }
}
