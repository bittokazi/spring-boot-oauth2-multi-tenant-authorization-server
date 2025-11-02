package com.bittokazi.oauth2.auth.frontend.frontend.app.secure

import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredModule
import com.bittokazi.kvision.spa.framework.base.layouts.DefaultLayoutLoader
import com.bittokazi.kvision.spa.framework.base.layouts.dashboard.layout.ContentContainerType
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.DashboardHomePage
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.account.accountModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.clientModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.roleModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.tenantModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.userModule
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine

fun dashboardModule() = DefaultSecuredModule(
    layoutLoader = DefaultLayoutLoader(),
    modules = listOf(
        clientModule(),
        userModule(),
        roleModule(),
        tenantModule(),
        accountModule()
    ),
    RouterConfiguration(
        route = AppEngine.APP_DASHBOARD_ROUTE,
        title = "Dashboard Home",
        view = {
            DashboardHomePage()
        },
        dashboardContainer = ContentContainerType.NO_CARD
    )
)