package com.bittokazi.oauth2.auth.frontend.frontend.base.services

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.services.LogoutActionProvider
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_LOGIN_ROUTE
import kotlinx.browser.window

class DefaultLogoutActionProvider: LogoutActionProvider {
    override fun logout(oauth2LoginPage: Boolean, fullLogout: Boolean) {
        if (fullLogout) {
            window.location.href = "/logout"
            return
        }
        when (oauth2LoginPage) {
            true -> window.location.href = "/oauth2/login"
            false -> SpaAppEngine.routing.navigate(APP_LOGIN_ROUTE)
        }
    }
}