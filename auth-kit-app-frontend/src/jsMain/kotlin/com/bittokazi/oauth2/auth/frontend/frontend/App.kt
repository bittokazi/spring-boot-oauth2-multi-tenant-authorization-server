package com.bittokazi.oauth2.auth.frontend.frontend

import com.bittokazi.kvision.spa.framework.base.common.ApplicationConfiguration
import com.bittokazi.kvision.spa.framework.base.common.AuthHolderType
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.common.SpaApplication
import com.bittokazi.kvision.spa.framework.base.models.SpaTenantInfo
import com.bittokazi.kvision.spa.framework.base.utils.importDefaultResources
import com.bittokazi.oauth2.auth.frontend.frontend.app.rootModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.menu.DefaultMenuProvider
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.AuthService
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.DefaultLogoutActionProvider

fun main() {

    importDefaultResources()
    SpaApplication.init()
    SpaAppEngine.restService.REFRESH_TOKEN_ENDPOINT = "${SpaAppEngine.restService.BASE_URL}/oauth2/refresh/token"

    AppEngine.restService = SpaAppEngine.restService
    AppEngine.authService = AuthService()
    AppEngine.tenantService = TenantService()

    SpaApplication.applicationConfiguration = ApplicationConfiguration(
        spaTenantInfo = SpaTenantInfo(
            cpanel = false,
            enabledConfigPanel = false,
            name = "SpaApplication"
        ),
        isTenantEnabled = true,
        rootApplicationModule = rootModule(),
        tenantInformationProvider = AppEngine.tenantService,
        authHolderType = AuthHolderType.COOKIE,
        menuProvider = DefaultMenuProvider(),
        logoutActionProvider = DefaultLogoutActionProvider(),
        refreshTokenRequestProvider = AppEngine.authService
    )

    SpaApplication.start()
}
