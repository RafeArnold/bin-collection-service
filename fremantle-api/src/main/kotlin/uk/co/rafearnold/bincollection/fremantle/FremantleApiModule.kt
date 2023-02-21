package uk.co.rafearnold.bincollection.fremantle

import com.google.inject.AbstractModule
import com.google.inject.Scopes

class FremantleApiModule : AbstractModule() {

    override fun configure() {
        bind(FremantleBinCollectionApiClient::class.java)
            .to(FremantleBinCollectionApiClientImpl::class.java).`in`(Scopes.SINGLETON)
    }
}
