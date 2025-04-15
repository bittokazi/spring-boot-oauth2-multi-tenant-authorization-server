package com.bittokazi.oauth2.auth.frontend.frontend

import com.bittokazi.oauth2.auth.frontend.frontend.app.appModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.authHolderType
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AuthHolderType
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AuthInformationHolder
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.DefaultAuthHolder
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.spinner.spinnerComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.AuthService
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.FileService
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.RestService
import io.kvision.Application
import io.kvision.CoreModule
import io.kvision.DatetimeModule
import io.kvision.RichTextModule
import io.kvision.ImaskModule
import io.kvision.ToastifyModule
import io.kvision.FontAwesomeModule
import io.kvision.BootstrapIconsModule
import io.kvision.MapsModule
import io.kvision.core.Background
import io.kvision.core.Color
import io.kvision.core.Position
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.style
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.i18n.DefaultI18nManager
import io.kvision.i18n.I18n
import io.kvision.module
import io.kvision.panel.root
import io.kvision.require
import io.kvision.rest.RemoteRequestException
import io.kvision.routing.Routing
import io.kvision.routing.Strategy
import io.kvision.startApplication
import kotlinx.browser.document

class App : Application() {
    override fun start() {
        I18n.manager =
            DefaultI18nManager(
                mapOf(
                    "pl" to require("i18n/messages-pl.json"),
                    "en" to require("i18n/messages-en.json")
                )
            )

        val routing = Routing.init("/", useHash = false, strategy = Strategy.ALL)

        AppEngine.routing = routing

        AppEngine.defaultAuthHolder = when (authHolderType) {
            AuthHolderType.LOCAL_STORAGE -> DefaultAuthHolder()
            AuthHolderType.COOKIE -> AuthInformationHolder()
        }

        AppEngine.defaultTenantHolder = AuthInformationHolder()

        AppEngine.restService = RestService()
        AppEngine.authService = AuthService()
        AppEngine.fileService = FileService()

        AppEngine.routing.resolve()
        root("kvapp") {
            val spinner = Div()
            spinner.id = "global-spinner"
            spinner.add(spinnerComponent())
            spinner.style {
                zIndex = 100000;
                position = Position.FIXED
                height = 100 to UNIT.perc
                width = 100 to UNIT.perc
                background = Background(Color("#f5f7fb"))
            }
            add(spinner)
            AppEngine.globalSpinnerObservable.subscribe {
                if(it)
                    spinner.getElementJQuery()?.fadeIn(10, "linear")
                else
                    spinner.getElementJQuery()?.fadeOut(500, "linear")
            }

            val container = Div()
            add(container)
            container.addAfterInsertHook {
                AppEngine.routing.updatePageLinks()
            }
            appModule(container)
            container.hide()
            TenantService.getCompanyInfo().then {
                when (it.data.enabledConfigPanel) {
                    true -> {
                        TenantService.tenantInfo = it.data
                        TenantService.tenantInfoObserver.setState(it.data)
                        container.show()
                    }
                    false -> {
                        div {
                            content = "\uD83D\uDEAB Access Denied"
                        }
                    }
                }
            }.catch { throwable ->
                if(throwable is RemoteRequestException) {
                    if(throwable.code.toInt() == 404) {
                        div {
                            content = "404 | Not Found"
                        }
                    } else {
                        div {
                            content = "503 | Service Unavailable"
                        }
                    }
                    AppEngine.globalSpinnerObservable.setState(false)
                }
            }
            return@root
        }

        AppEngine.pageTitleObserver.setState("Cpanel")

        AppEngine.pageTitleObserver.subscribe {
            document.title = it
        }
    }
}

fun main() {
    startApplication(
        ::App,
        module.hot,
        //BootstrapModule,
        //BootstrapCssModule,
        DatetimeModule,
        RichTextModule,
        //BootstrapUploadModule,
        ImaskModule,
        ToastifyModule,
        FontAwesomeModule,
        BootstrapIconsModule,
        MapsModule,
        CoreModule
    )
}
