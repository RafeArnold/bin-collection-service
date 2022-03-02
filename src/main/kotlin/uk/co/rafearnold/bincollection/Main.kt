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

fun main(vararg args: String) {
    val properties = Properties()
    val propertiesFile: String =
        args.getOrNull(0) ?: System.getProperty("application.properties.path") ?: "application.properties"
    ClassLoader.getSystemResource(propertiesFile).openStream().use { properties.load(it) }
    val logbackFile: String? = properties.getProperty("logback.configurationFile")
    if (logbackFile != null) System.setProperty("logback.configurationFile", logbackFile)
    val log: Logger = LoggerFactory.getLogger("uk.co.rafearnold.bincollection.MainKt")
    runCatching {
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
