package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.account

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.account.components.myProfileComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.account.components.securitySettingsComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.ContentContainerType
import io.kvision.core.Container

fun Container.accountModule(layoutContainer: Container) {
    AppEngine.routing
        .on(APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(myProfileComponent())
                AppEngine.dashboardContentContainerTypeObserver.setState(ContentContainerType.NO_CARD)
                AppEngine.pageTitleObserver.setState("Account")
            }
        })
        .on(APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(securitySettingsComponent())
                AppEngine.dashboardContentContainerTypeObserver.setState(ContentContainerType.NO_CARD)
                AppEngine.pageTitleObserver.setState("Security Settings")
            }
        })
}