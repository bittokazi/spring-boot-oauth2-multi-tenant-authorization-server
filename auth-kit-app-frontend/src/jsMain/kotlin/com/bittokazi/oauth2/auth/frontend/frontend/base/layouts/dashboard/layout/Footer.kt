package com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import io.kvision.core.Container
import io.kvision.html.*

fun Container.dashboardFooter() {
    footer(className = "footer") {
        div(className = "container-fluid") {
            div(className = "row text-muted") {
                div(className = "col-6 text-start") {
                    link("", AppEngine.APP_DASHBOARD_ROUTE, dataNavigo = true) {
                        add(strong {
                            content = TenantService.tenantInfo.name
                        })
                    }
                    add(span {
                        content = "&nbsp; &copy;"
                        rich = true
                    })
                }
                div(className = "col-6 text-end") {
                    ul(className = "list-inline") {
                        li(className = "list-inline-item") {
                            link(TenantService.tenantInfo.systemVersion, className = "text-muted")
                        }
                    }
                }
            }
        }
    }
}
