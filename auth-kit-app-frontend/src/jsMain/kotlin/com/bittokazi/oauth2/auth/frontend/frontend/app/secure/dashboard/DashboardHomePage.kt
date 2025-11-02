package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import io.kvision.core.UNIT
import io.kvision.html.TAG
import io.kvision.html.div
import io.kvision.html.h5
import io.kvision.html.image
import io.kvision.html.li
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.tag
import io.kvision.html.ul
import io.kvision.panel.SimplePanel

class DashboardHomePage(): SimplePanel() {
    init {
        div(className = "row") {
            div(className = "col-md-4 col-xl-3") {
                div(className = "card mb-3") {
                    div(className = "card-header") {
                        h5(className = "card-title mb-0", content = "Profile Details")
                    }
                    div(className = "card-body text-center") {
                        image(
                            src = AppEngine.authService.user!!.avatarImage,
                            className = "img-fluid rounded-circle mb-2"
                        ) {
                            width = 128 to UNIT.px
                            height = 128 to UNIT.px
                        }
                        h5(className = "card-title mb-0") {
                            content = "${AppEngine.authService.user!!.username}"
                        }
                        div(className = "text-muted mb-2") {
                            content = AppEngine.authService.user?.roles?.firstOrNull()?.title
                        }
                    }
                    tag(TAG.HR, className = "my-0")
                    div(className = "card-body") {
                        h5(content = "About", className = "h6 card-title")
                        ul(className = "list-unstyled mb-0") {
                            li(className = "mb-1") {
                                span(className = "feather-sm me-1") {
                                    setAttribute("data-feather", "mail")
                                }
                                + "Email "
                                link(
                                    label = AppEngine.authService.user!!.email!!,
                                    url = "/app/dashboard",
                                    dataNavigo = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
