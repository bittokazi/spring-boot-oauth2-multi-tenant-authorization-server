package com.bittokazi.oauth2.auth.frontend.frontend.base.services

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_LOGIN_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.ContentContainerType
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.rest.*
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlin.js.Promise

class AuthService {

    var user: User? = null
    val authObservableValue: ObservableValue<User?> = ObservableValue(null)
    private val restService: RestService = AppEngine.restService
    var block: ()-> Unit = {}

    fun whoAmI(): Promise<RestResponse<User>> {
        return AppEngine.restService.createAuthCall {
            restService.getClient().request<User>("${restService.API}/users/whoami") {
                method = HttpMethod.GET
            }
        }
    }

    fun logout(
        oauth2LoginPage: Boolean = false,
        fullLogout: Boolean = false
    ) {
        AppEngine.defaultTenantHolder.setTenant(null)
        AppEngine.defaultAuthHolder.setAuth(null)
        AppEngine.globalSpinnerObservable.setState(true)
        window.document.cookie = "access_token=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;"

        if (fullLogout) {
            window.location.href = "/logout"
            return
        }
        when (oauth2LoginPage) {
            true -> window.location.href = "/oauth2/login"
            false -> AppEngine.routing.navigate(APP_LOGIN_ROUTE)
        }
    }

    fun authenticated(_block: ()-> Unit) {
        AppEngine.dashboardContentContainerTypeObserver.setState(ContentContainerType.CARD)
        block = _block
    }

    fun open() {
        block()
        block = {}
    }

    fun switchTenant(tenant: String?) {
        AppEngine.defaultTenantHolder.setTenant(tenant)
        AppEngine.defaultAuthHolder.setAuth(null)
        AppEngine.globalSpinnerObservable.setState(true)
        AppEngine.authService.user = null
        AppEngine.authObserver.setState(false)
        AppEngine.routing.navigate(APP_LOGIN_ROUTE)
    }
}
