package com.bittokazi.oauth2.auth.cdn

import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.mustache.MustacheContent
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        staticResources("/static", "static")

        get("/app/{...}") {
            call.respond(MustacheContent("cpanel.hbs", mapOf<String, String>()))
        }
    }
}
