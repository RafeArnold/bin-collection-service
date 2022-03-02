package uk.co.rafearnold.bincollection

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import uk.co.rafearnold.bincollection.cambridge.CambridgeApiModule
import uk.co.rafearnold.bincollection.cambridge.CambridgeBinCollectionService
import uk.co.rafearnold.bincollection.cambridge.CambridgeBinCollectionServiceImpl
import uk.co.rafearnold.bincollection.model.ModelFactory
import uk.co.rafearnold.bincollection.model.ModelFactoryImpl

class InternalApiModule : AbstractModule() {

    override fun configure() {
        install(CambridgeApiModule())
        bind(BinCollectionService::class.java).to(BinCollectionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ModelFactory::class.java).to(ModelFactoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CommandParser::class.java).to(CommandParserImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CambridgeBinCollectionService::class.java)
            .to(CambridgeBinCollectionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        // Don't want this to be a singleton, to ensure a new instance is injected each time.
        bind(AsyncLockManager::class.java).to(AsyncLockManagerImpl::class.java)
    }
}
