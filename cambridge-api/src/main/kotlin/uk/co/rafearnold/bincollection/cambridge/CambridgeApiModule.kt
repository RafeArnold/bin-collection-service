package uk.co.rafearnold.bincollection.cambridge

import com.google.inject.AbstractModule
import com.google.inject.Scopes

class CambridgeApiModule : AbstractModule() {

    override fun configure() {
        bind(CambridgeBinCollectionApiClient::class.java)
            .to(CambridgeBinCollectionApiClientImpl::class.java).`in`(Scopes.SINGLETON)
    }
}
