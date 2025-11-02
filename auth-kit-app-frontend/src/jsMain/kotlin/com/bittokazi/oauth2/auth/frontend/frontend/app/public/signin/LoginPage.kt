package com.bittokazi.oauth2.auth.frontend.frontend.app.public.signin

import com.bittokazi.kvision.spa.framework.base.common.ObservableManager
import com.bittokazi.kvision.spa.framework.base.services.SpaTenantService.tenantInfoObserver
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.BACKEND_OAUTH2_LOGIN_ROUTE
import io.kvision.core.getElementJQuery
import io.kvision.form.form
import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.browser.window

private const val COMPANY_INFO_OBSERVER_LOGIN_PAGE = "companyInfoObserverLoginPage"

class LoginPage(): SimplePanel() {

    private val errorMessage: ObservableValue<String> = ObservableValue("")

    init {
        var tenantName = "CPanel"

        var errorDiv: Div? = null

        errorMessage.subscribe {
            if (errorDiv != null && errorDiv?.visible == false) {
                errorDiv?.show()
                errorDiv?.getElementJQuery()?.hide(0)
            }
            errorDiv?.getElementJQuery()?.slideToggle(300).also {_it ->
                when (it) {
                    "" -> errorDiv?.getElementJQuery()?.fadeOut(300, "linear")
                    else -> {
                        errorDiv?.getElementJQuery()?.fadeIn(300, "linear")
                    }
                }
            }
        }

        main(className = "d-flex w-100") {
            div(className = "container d-flex flex-column") {
                div(className = "row vh-100") {
                    div(className = "col-sm-10 col-md-8 col-lg-6 col-xl-5 mx-auto d-table h-100") {
                        div(className = "d-table-cell align-middle") {
                            div(className = "text-center mt-4") {
                                h1(className = "h2").bind(tenantInfoObserver) {
                                    if (it != null) {
                                        tenantName = it.name
                                    }
                                    content = tenantName
                                }
                                p("Sign in to your account to continue", className = "lead")
                            }

                            div(className = "card") {
                                div(className = "card-body") {
                                    div(className = "m-sm-3") {
                                        form {
                                            ObservableManager.setSubscriber(COMPANY_INFO_OBSERVER_LOGIN_PAGE) {
                                                tenantInfoObserver.subscribe {
                                                    if(it != null) {
                                                        div(className = "d-grid gap-2 mt-3") {
                                                            button(
                                                                "Login using SSO",
                                                                type = ButtonType.BUTTON,
                                                                className = "btn btn-lg btn-danger"
                                                            ).onClick {
                                                                window.location.href = BACKEND_OAUTH2_LOGIN_ROUTE
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
