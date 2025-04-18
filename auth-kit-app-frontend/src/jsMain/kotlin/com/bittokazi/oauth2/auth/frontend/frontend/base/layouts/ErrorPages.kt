package com.bittokazi.oauth2.auth.frontend.frontend.base.layouts

import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.h1
import io.kvision.html.p

fun Container.errorPage(titleText: String, bodyText: String) {
    div(className = "custom-bg text-dark") {
        div(className = "d-flex align-items-center justify-content-center min-vh-100 px-2") {
            div(className = "text-center") {
                h1(titleText, className = "display-1 fw-bold")
                p(bodyText, className = "fs-2 fw-medium mt-4")
            }
        }
    }
}
