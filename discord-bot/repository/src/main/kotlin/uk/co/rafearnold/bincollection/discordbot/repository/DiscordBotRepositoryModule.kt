package uk.co.rafearnold.bincollection.discordbot.repository

import com.google.inject.AbstractModule
import com.google.inject.Scopes

class DiscordBotRepositoryModule : AbstractModule() {

    override fun configure() {
        bind(UserInfoRepository::class.java).to(RedisUserInfoRepository::class.java).`in`(Scopes.SINGLETON)
    }
}
