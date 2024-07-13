package com.bittokazi.oauth2.auth.server

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
        logger.info("\uD83D\uDE80 Application started.")
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
