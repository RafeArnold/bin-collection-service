package uk.co.rafearnold.bincollection

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.bincollection.cambridge.CambridgeBinCollectionService
import uk.co.rafearnold.bincollection.fremantle.FremantleBinCollectionService
import uk.co.rafearnold.bincollection.model.CambridgeAddressInfo
import uk.co.rafearnold.bincollection.model.FremantleAddressInfo
import uk.co.rafearnold.bincollection.model.NextBinCollection
import uk.co.rafearnold.bincollection.model.NotificationSubscriptionSettings
import uk.co.rafearnold.bincollection.model.NotificationTimeSetting
import uk.co.rafearnold.bincollection.model.AddressInfo
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BinCollectionServiceImpl @Inject constructor(
    private val fremantleService: FremantleBinCollectionService,
    private val cambridgeService: CambridgeBinCollectionService
) : BinCollectionService {

    private val notificationSubscriptions: MutableMap<String, NotificationSubscriptionSettings> = mutableMapOf()

    override fun subscribeToNextBinCollectionNotifications(
        addressInfo: AddressInfo,
        notificationTimes: Set<NotificationTimeSetting>,
        notificationHandler: Handler<NextBinCollection>
    ): CompletableFuture<String> =
        CompletableFuture.supplyAsync {
            val subscriptionId: String = UUID.randomUUID().toString()
            val runnable =
                Runnable {
                    CompletableFuture.completedFuture(null).thenCompose {
                        log.info("Retrieving next bin collection for subscription '$subscriptionId'")
                        getNextBinCollection(addressInfo = addressInfo)
                            .thenAccept { nextBinCollection: NextBinCollection ->
                                val isCorrectDay: Boolean =
                                    LocalDate.now() in
                                            notificationTimes.map {
                                                nextBinCollection.dateOfCollection.minusDays(it.daysBeforeCollection.toLong())
                                            }
                                if (isCorrectDay) notificationHandler.handle(nextBinCollection)
                            }
                    }.exceptionally {
                        log.error("Error encountered while handling bin collection notification", it)
                        null
                    }
                }
            val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
            for (notificationTimeSetting: NotificationTimeSetting in notificationTimes) {
                val notificationTime: LocalTime =
                    LocalTime.of(notificationTimeSetting.hourOfDay, notificationTimeSetting.minuteOfHour)
                // Seconds in a day.
                val periodSeconds: Long = 86400
                val initialDelaySeconds: Long =
                    Duration.between(LocalTime.now(), notificationTime)
                        .let { if (it.isNegative) it.plusSeconds(periodSeconds) else it }
                        .seconds
                executor.scheduleAtFixedRate(runnable, initialDelaySeconds, periodSeconds, TimeUnit.SECONDS)
            }
            notificationSubscriptions[subscriptionId] = NotificationSubscriptionSettings(executor = executor)
            subscriptionId
        }

    override fun unsubscribeFromNextBinCollectionNotifications(subscriptionId: String): CompletableFuture<Void> =
        CompletableFuture.runAsync { notificationSubscriptions.remove(subscriptionId)?.executor?.shutdown() }

    override fun getNextBinCollection(addressInfo: AddressInfo): CompletableFuture<NextBinCollection> =
        when (addressInfo) {
            is CambridgeAddressInfo ->
                cambridgeService.getNextBinCollection(postcode = addressInfo.postcode, houseNumber = addressInfo.houseNumber)

            is FremantleAddressInfo -> fremantleService.getNextBinCollection(addressQuery = addressInfo.addressQuery)
        }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BinCollectionServiceImpl::class.java)
    }
}
