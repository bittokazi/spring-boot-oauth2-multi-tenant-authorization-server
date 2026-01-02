package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client

import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredPageModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.ClientAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.ClientListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.ClientUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine

fun clientModule() = DefaultSecuredPageModule(
    RouterConfiguration(
        route = AppEngine.APP_DASHBOARD_CLIENT_ADD_ROUTE,
        title = "Add Client",
        view = {
            ClientAddComponent()
        }
    ),
    RouterConfiguration(
        route = AppEngine.APP_DASHBOARD_CLIENT_UPDATE_ROUTE(":id"),
        title = "Update Client",
        view = {
            ClientUpdateComponent(it.data.id)
        }
    ),
    RouterConfiguration(
        route = AppEngine.APP_DASHBOARD_CLIENT_ROUTE,
        title = "All Clients",
        view = {
            ClientListComponent()
        }
    )
)
