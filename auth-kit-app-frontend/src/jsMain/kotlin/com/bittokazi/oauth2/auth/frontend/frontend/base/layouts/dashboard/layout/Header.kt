package com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.ObservableManager
import io.kvision.core.Container
import io.kvision.html.*

private const val TOP_BAR_MENU_CLICK = "topBarMenuCLick"

fun Container.dashboardHeader() {
    val nav = Nav(className = "sidebar js-sidebar collapsed") {
        id="sidebar"
        div(className = "sidebar-content js-simplebar") {
            link("", AppEngine.APP_DASHBOARD_ROUTE, className = "sidebar-brand", dataNavigo = true) {
                add(span(className = "align-middle", content = TenantService.tenantInfo.name))
            }
            dashboardMenuBar()
        }
    }
    add(nav)

    ObservableManager.setSubscriber(TOP_BAR_MENU_CLICK) {
        AppEngine.dashboardPageChangeObserver.subscribe {
            nav.addCssClass("collapsed")
            nav.removeCssClass("collapsed")
        }
    }
}
