package com.bittokazi.oauth2.auth.cdn

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.server.application.*
import io.ktor.server.mustache.Mustache

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }

    configureRouting()
}
