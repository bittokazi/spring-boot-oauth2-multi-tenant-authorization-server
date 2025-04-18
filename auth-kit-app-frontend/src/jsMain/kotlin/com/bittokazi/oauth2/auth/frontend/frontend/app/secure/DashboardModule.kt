package com.bittokazi.oauth2.auth.frontend.frontend.app.secure

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.account.accountModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.clientModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.dashboardHomePage
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.roleModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.tenantModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.userModule
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.ContentContainerType
import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.dashboardContent
import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.dashboardFooter
import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.dashboardHeader
import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.dashboardTopBar
import io.kvision.core.Container
import io.kvision.html.Div
import io.kvision.html.div

fun Container.dashboardModule(parentContainer: Container) {

    val container = Div()
    container.addAfterInsertHook {
        AppEngine.routing.updatePageLinks()
    }

    val layout = Div()
    layout.addAfterInsertHook {
        AppEngine.routing.updatePageLinks()
    }

    this.add(layout)

    AppEngine.authObserver.subscribe {
        if(it && layout.getChildren().isEmpty()) {
            layout.removeAll()
            layout.add(div(className = "wrapper") {
                dashboardHeader()
                div(className = "main") {
                    dashboardTopBar()
                    dashboardContent(container)
                    dashboardFooter()
                }
            })
            if (AppEngine.authService.user != null) {
                AppEngine.authService.open()
            }
        } else if (!it) {
            layout.removeAll()
        }
    }

    userModule(container)
    tenantModule(container)
    clientModule(container)
    roleModule(container)
    accountModule(container)

    AppEngine.routing
        .on(AppEngine.APP_DASHBOARD_ROUTE, {
            AppEngine.authService.authenticated {
                container.removeAll()
                container.add(dashboardHomePage())
                AppEngine.dashboardContentContainerTypeObserver.setState(ContentContainerType.NO_CARD)
                AppEngine.pageTitleObserver.setState("Dashboard Home")
            }
        })
}
