package uk.co.rafearnold.bincollection.guice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import redis.clients.jedis.Jedis
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import uk.co.rafearnold.bincollection.RedisClientProvider
import uk.co.rafearnold.bincollection.Register
import uk.co.rafearnold.bincollection.http.DefaultChannelHandlerFactory
import uk.co.rafearnold.bincollection.http.HttpServer
import uk.co.rafearnold.bincollection.http.LoggingChannelHandlerFactory

class MainModule(private val properties: Map<String, String>) : AbstractModule() {

    @Provides
    @Singleton
    fun applicationProperties(): Map<String, String> = properties

    @Provides
    @Singleton
    fun objectMapper(): ObjectMapper =
        jacksonObjectMapper()
            .registerModule(JavaTimeModule())

    override fun configure() {
        Names.bindProperties(binder(), properties)
        bind(Jedis::class.java).toProvider(RedisClientProvider::class.java).`in`(Scopes.SINGLETON)
        bindRegisters()
        bindChannelHandlerFactories()
    }

    private fun bindRegisters() {
        val multibinder: Multibinder<Register> = Multibinder.newSetBinder(binder(), Register::class.java)
        multibinder.addBinding().to(HttpServer::class.java).`in`(Scopes.SINGLETON)
    }

    private fun bindChannelHandlerFactories() {
        val multibinder: Multibinder<OrderedChannelHandlerFactory> =
            Multibinder.newSetBinder(binder(), OrderedChannelHandlerFactory::class.java)
        multibinder.addBinding().to(DefaultChannelHandlerFactory::class.java).`in`(Scopes.SINGLETON)
        multibinder.addBinding().to(LoggingChannelHandlerFactory::class.java).`in`(Scopes.SINGLETON)
    }
}
