package com.bittokazi.oauth2.auth.frontend.frontend.base.common

import com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout.ContentContainerType
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.AuthService
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.FileService
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.RestService
import io.kvision.navigo.Navigo
import io.kvision.state.ObservableValue

object AppEngine {
    lateinit var routing: Navigo
    lateinit var defaultAuthHolder: AuthHolder
    lateinit var defaultTenantHolder: TenantHolder
    lateinit var restService: RestService
    lateinit var authService: AuthService
    lateinit var fileService: FileService

    var authHolderType: AuthHolderType = AuthHolderType.COOKIE

    var globalSpinnerObservable: ObservableValue<Boolean> = ObservableValue(true)
    var authObserver: ObservableValue<Boolean> = ObservableValue(false)
    var pageTitleObserver: ObservableValue<String> = ObservableValue("")
    var dashboardPageChangeObserver: ObservableValue<String> = ObservableValue("")
    var dashboardContentContainerTypeObserver: ObservableValue<ContentContainerType> = ObservableValue(
        ContentContainerType.CARD
    )
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
