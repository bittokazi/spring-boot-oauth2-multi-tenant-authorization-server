package com.bittokazi.oauth2.auth.frontend.frontend.base.components.modal

import io.kvision.core.Container
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h1

fun Container.bootstrapModalComponent(
    modalId: String,
    title: String,
    contentBody: () -> Container,
    callback: (Map<String, String>) -> Unit,
): Container {
    return div(className = "modal fade") {
        setAttribute("id", modalId)
        div(className = "modal-dialog") {
            div(className = "modal-content") {
                div(className = "modal-header") {
                    h1(title, className = "modal-title fs-5")
                    button("", className = "btn-close",) {
                        setAttribute("id", "${modalId}CloseBtn")
                        setAttribute("data-bs-dismiss", "modal")
                        setAttribute("aria-label", "Close")
                    }
                }
                div(className = "modal-body") {
                    setAttribute("id", "${modalId}Body")
                    add(contentBody())
                }
            }
        }
    }
}

object BootstrapModalService {
    fun open(fn: () -> dynamic) {
        fn().show()
    }
}
