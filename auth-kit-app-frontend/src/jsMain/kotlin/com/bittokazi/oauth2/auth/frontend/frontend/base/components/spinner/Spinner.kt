package com.bittokazi.oauth2.auth.frontend.frontend.base.components.spinner

import io.kvision.core.*
import io.kvision.html.div
import io.kvision.html.p
import io.kvision.html.span

fun Container.spinnerComponent(): Container {
    return div(className = "") {
        zIndex = 100000;
        position = Position.FIXED
        height = 100 to UNIT.perc
        width = 100 to UNIT.perc
        background = Background(Color("#f5f7fb"))

        div(className = "spinner-border text-primary") {
            left = 50 to UNIT.perc
            top = 50 to UNIT.perc
            position = Position.RELATIVE
            marginLeft = -16 to UNIT.px
            marginTop = -50 to UNIT.px
            setAttribute("role", "status")
            span(className = "visually-hidden") {
                content = "Loading..."
            }
        }
        p {
            content = "Loading..."
            textAlign = TextAlign.CENTER
            padding = 15 to UNIT.px
            fontSize = 15 to UNIT.px
            top = 50 to UNIT.perc
            position = Position.RELATIVE
        }
    }
}
