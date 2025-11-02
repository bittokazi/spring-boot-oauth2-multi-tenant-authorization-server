package com.bittokazi.oauth2.auth.frontend.frontend.base.common

import com.bittokazi.kvision.spa.framework.base.services.RestService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.AuthService
import io.kvision.state.ObservableValue

object AppEngine {
    lateinit var restService: RestService
    lateinit var authService: AuthService
    lateinit var tenantService: TenantService

    var userInfoChangeObserver: ObservableValue<User?> = ObservableValue(null)

    const val APP_BASE_ROUTE = "/app"
    const val APP_DASHBOARD_ROUTE = "$APP_BASE_ROUTE/dashboard"

    const val APP_LOGIN_ROUTE = "$APP_BASE_ROUTE/login"

    const val APP_DASHBOARD_TENANT_ROUTE = "$APP_DASHBOARD_ROUTE/tenants"
    const val APP_DASHBOARD_TENANT_ADD_ROUTE = "$APP_DASHBOARD_TENANT_ROUTE/add"
    val APP_DASHBOARD_TENANT_UPDATE_ROUTE: (identifier: String) -> String = {
        "$APP_DASHBOARD_TENANT_ROUTE/$it/update"
    }

    const val APP_DASHBOARD_ROLE_ROUTE = "$APP_DASHBOARD_ROUTE/roles"
    const val APP_DASHBOARD_ROLE_ADD_ROUTE = "$APP_DASHBOARD_ROLE_ROUTE/add"
    val APP_DASHBOARD_ROLE_UPDATE_ROUTE: (identifier: String) -> String = {
        "$APP_DASHBOARD_ROLE_ROUTE/$it/update"
    }

    const val APP_DASHBOARD_CLIENT_ROUTE = "$APP_DASHBOARD_ROUTE/clients"
    const val APP_DASHBOARD_CLIENT_ADD_ROUTE = "$APP_DASHBOARD_CLIENT_ROUTE/add"
    val APP_DASHBOARD_CLIENT_UPDATE_ROUTE: (identifier: String) -> String = {
        "$APP_DASHBOARD_CLIENT_ROUTE/$it/update"
    }

    const val APP_DASHBOARD_USER_ROUTE = "$APP_DASHBOARD_ROUTE/users"
    const val APP_DASHBOARD_USER_ADD_ROUTE = "$APP_DASHBOARD_USER_ROUTE/add"
    val APP_DASHBOARD_USER_UPDATE_ROUTE: (identifier: String) -> String = {
        "$APP_DASHBOARD_USER_ROUTE/$it/update"
    }

    const val APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE = "$APP_DASHBOARD_ROUTE/account-settings"
    const val APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE = "$APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE/security"

    const val BACKEND_OAUTH2_LOGIN_ROUTE = "/oauth2/login"
}
