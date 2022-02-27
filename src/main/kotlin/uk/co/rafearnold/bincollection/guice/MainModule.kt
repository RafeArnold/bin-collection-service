package uk.co.rafearnold.bincollection.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.Singleton
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import uk.co.rafearnold.bincollection.BinCollectionService
import uk.co.rafearnold.bincollection.BinCollectionServiceImpl
import uk.co.rafearnold.bincollection.CommandParser
import uk.co.rafearnold.bincollection.CommandParserImpl
import uk.co.rafearnold.bincollection.OrderedChannelHandlerFactory
import uk.co.rafearnold.bincollection.Register
import uk.co.rafearnold.bincollection.http.DefaultChannelHandlerFactory
import uk.co.rafearnold.bincollection.http.HttpServer
import uk.co.rafearnold.bincollection.http.LoggingChannelHandlerFactory
import uk.co.rafearnold.bincollection.model.BackendModelFactory
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.ModelFactoryImpl

class MainModule(private val properties: Map<String, String>) : AbstractModule() {

    @Provides
    @Singleton
    fun applicationProperties(): Map<String, String> = properties

    override fun configure() {
        Names.bindProperties(binder(), properties)
        bind(BinCollectionService::class.java).to(BinCollectionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ModelFactory::class.java).to(ModelFactoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(BackendModelFactory::class.java).to(ModelFactoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CommandParser::class.java).to(CommandParserImpl::class.java).`in`(Scopes.SINGLETON)
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
