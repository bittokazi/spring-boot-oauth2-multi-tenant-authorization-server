package com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.state.bind

fun Container.dashboardTopBar() {
    nav(className = "navbar navbar-expand navbar-light navbar-bg") {
        link("", className = "sidebar-toggle js-sidebar-toggle") {
            add(i(className = "hamburger align-self-center"))
        }
        div(className = "navbar-collapse collapse") {
            ul(className = "navbar-nav navbar-align") {
                li(className = "nav-item dropdown") {
                    link("", "#", className = "nav-icon dropdown-toggle d-inline-block d-sm-none") {
                        setAttribute("data-bs-toggle", "dropdown")
                        add(i(className = "align-middle") {
                            setAttribute("data-feather", "settings")
                        })
                    }
                    link("", "#", className = "nav-link dropdown-toggle d-none d-sm-inline-block") {
                        setAttribute("data-bs-toggle", "dropdown")
                        add(
                            image(
                                AppEngine.authService.user?.avatarImage,
                                AppEngine.authService.user?.email,
                                className = "avatar img-fluid rounded me-1"
                            )
                        )
                        add(span(className = "text-dark").bind(AppEngine.userInfoChangeObserver) {
                            content = "${AppEngine.authService.user?.email}"
                        })
                    }
                    div(className = "dropdown-menu dropdown-menu-end") {
                        link("", APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE, className = "dropdown-item", dataNavigo = true) {
                            add(i(className = "align-middle me-1") {
                                setAttribute("data-feather", "user")
                            })
                            add(span {
                                content = "Account"
                            })
                            if (!AppEngine.defaultTenantHolder.getTenant().isNullOrEmpty()) hide()
                        }
                        div(className = "dropdown-divider") {
                            if (!AppEngine.defaultTenantHolder.getTenant().isNullOrEmpty()) hide()
                        }
                        link("", APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE, className = "dropdown-item", dataNavigo = true) {
                            add(i(className = "align-middle me-1") {
                                setAttribute("data-feather", "settings")
                            })
                            add(span {
                                content = "Settings"
                            })
                            if (!AppEngine.defaultTenantHolder.getTenant().isNullOrEmpty()) hide()
                        }
                        link("", className = "dropdown-item") {
                            add(i(className = "align-middle me-1") {
                                setAttribute("data-feather", "log-in")
                            })
                            add(span {
                                content = "Switch Back"
                            })
                            onClick {
                                AppEngine.authService.switchTenant(null)
                            }
                            if (AppEngine.defaultTenantHolder.getTenant().isNullOrEmpty()) hide()
                        }
                        div(className = "dropdown-divider")
                        link("", className = "dropdown-item") {
                            add(span {
                                content = "Logout"
                            })
                        }.onClick {
                            AppEngine.authService.logout(fullLogout = true)
                        }
                    }
                }
            }
        }
    }
}
