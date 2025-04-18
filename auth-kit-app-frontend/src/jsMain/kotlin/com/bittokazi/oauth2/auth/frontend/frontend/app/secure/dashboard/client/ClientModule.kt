package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.clientAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.clientListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.clientUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_CLIENT_ADD_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_CLIENT_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_CLIENT_UPDATE_ROUTE
import io.kvision.core.Container

fun Container.clientModule(layoutContainer: Container) {
    AppEngine.routing
        .on(APP_DASHBOARD_CLIENT_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(clientListComponent())
                AppEngine.pageTitleObserver.setState("All Clients")
            }
        })
        .on(APP_DASHBOARD_CLIENT_ADD_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(clientAddComponent())
                AppEngine.pageTitleObserver.setState("Add Client")
            }
        })
        .on(APP_DASHBOARD_CLIENT_UPDATE_ROUTE(":id"), {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(clientUpdateComponent(it.data.id))
                AppEngine.pageTitleObserver.setState("Update Client")
            }
        })
}
