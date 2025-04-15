package com.bittokazi.oauth2.auth.frontend.frontend.app

import com.bittokazi.oauth2.auth.frontend.frontend.app.public.HomePage
import com.bittokazi.oauth2.auth.frontend.frontend.app.public.signin.LoginPage
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService.tenantInfoObserver
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboardModule
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.ObservableManager
import io.kvision.core.Container
import kotlinx.browser.window
import org.w3c.dom.get

private const val COMPANY_INFO_OBSERVER_LOGIN_PAGE_TITLE = "companyInfoObserverLoginPageTitle"

fun Container.appModule(parentContainer: Container) {
    val authHolder = AppEngine.defaultAuthHolder

    dashboardModule(parentContainer)

    AppEngine.routing
        .on("/app", {
            parentContainer.removeAll()
            parentContainer.add(HomePage())
            window.setTimeout({
                AppEngine.globalSpinnerObservable.setState(false)
            }, 1000)
            AppEngine.authObserver.setState(false)
            AppEngine.pageTitleObserver.setState("App Home")
        })
        .on("/app/login", {
            parentContainer.removeAll()
            when(authHolder.getAuth()) {
                null ->  {
                    parentContainer.add(LoginPage())
                    window.setTimeout({
                        AppEngine.globalSpinnerObservable.setState(false)
                    }, 1000)
                    AppEngine.authObserver.setState(false)
                    AppEngine.pageTitleObserver.setState("Login")
                }
                else -> {
                    AppEngine.routing.navigate(AppEngine.APP_DASHBOARD_ROUTE)
                }
            }
            ObservableManager.setSubscriber(COMPANY_INFO_OBSERVER_LOGIN_PAGE_TITLE) {
                tenantInfoObserver.subscribe {
                    if(it != null) {
                        AppEngine.pageTitleObserver.setState("Login | ${TenantService.tenantInfo.name}")
                    }
                }
            }
        })
        .on("/app/dashboard/*", {
            parentContainer.removeAll()
            when(authHolder.getAuth()) {
                null -> AppEngine.routing.navigate(AppEngine.APP_LOGIN_ROUTE)
                else -> {
                    if (AppEngine.authService.user == null || !AppEngine.authObserver.value) {
                        AppEngine.globalSpinnerObservable.setState(true)
                        AppEngine.authService.whoAmI().then {
                            AppEngine.authService.user = it.data
                            AppEngine.authService.authObservableValue.setState(it.data)
                            window.setTimeout({
                                AppEngine.globalSpinnerObservable.setState(false)
                                window["feather"].replace()
                                window["sidebarInit"]()
                            }, 1000)
                            AppEngine.authObserver.setState(true)
                        }.catch {
                            AppEngine.authService.logout()
                        }
                    } else {
                        AppEngine.authObserver.setState(true)
                    }
                }
            }
        }).addAfterHook("/app/dashboard/*") {
            if (AppEngine.authService.user != null) {
                AppEngine.authService.open()
            }
            AppEngine.dashboardPageChangeObserver.setState("urlChanged")
            window.setTimeout({
                window["feather"].replace()
            }, 100)
        }
}
