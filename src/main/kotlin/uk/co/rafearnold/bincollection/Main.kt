package uk.co.rafearnold.bincollection

import com.google.inject.Guice
import com.google.inject.Injector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.bincollection.discordbot.DiscordBotModule
import uk.co.rafearnold.bincollection.guice.MainModule
import uk.co.rafearnold.bincollection.messengerbot.MessengerBotModule
import uk.co.rafearnold.bincollection.restapiv1.RestApiV1Module
import java.util.*
import kotlin.system.exitProcess

fun main() {
    val log: Logger = LoggerFactory.getLogger("uk.co.rafearnold.bincollection.MainKt")
    runCatching {
        val properties = Properties()
        val propertiesFile: String = System.getProperty("application.properties.path") ?: "application.properties"
        ClassLoader.getSystemResource(propertiesFile).openStream().use { properties.load(it) }
        val propertiesMap: MutableMap<String, String> = mutableMapOf()
        for ((key: Any?, value: Any?) in properties) if (key is String && value is String) propertiesMap[key] = value
        val injector: Injector =
            Guice.createInjector(
                MainModule(properties = propertiesMap),
                InternalApiModule(),
                RestApiV1Module(),
                DiscordBotModule(),
                MessengerBotModule()
            )
        injector.getInstance(RegisterService::class.java).register().get()
        log.info("Application successfully launched")
    }.onFailure {
        log.error("Application failed to launch", it)
        exitProcess(-1)
    }
}
