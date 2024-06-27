package com.bittokazi.api.gateway

import com.bittokazi.api.gateway.utils.logger
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
        logger.info("\uD83D\uDE80 Gateway Application started.")
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
