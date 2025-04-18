package com.bittokazi.oauth2.auth.server

import com.bittokazi.oauth2.auth.server.config.AppConfig
import com.bittokazi.oauth2.auth.server.utils.Utils
import com.bittokazi.oauth2.auth.server.utils.logger
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener

@SpringBootApplication
class Application {
    companion object {
        val logger = logger()
    }

    @EventListener(ApplicationReadyEvent::class)
    fun afterStartup() {
        AppConfig.VERSION = Utils.readVersion()
        logger.info("\uD83D\uDE80 Application started [${AppConfig.VERSION}]")
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
