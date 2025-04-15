package com.bittokazi.oauth2.auth.frontend.frontend.base.layouts.dashboard.layout

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.ObservableManager
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import io.kvision.core.Container
import io.kvision.html.*
import kotlinx.browser.window
import kotlin.collections.get

fun Container.dashboardMenuBar() {

    val menuItems = AppEngine.authService.user?.roles?.get(0)?.let { getMenu(it) }?:run { listOf() }
    val items: MutableMap<String, Li> = mutableMapOf()
    val parents: MutableMap<String, Li> = mutableMapOf()

    ul(className = "sidebar-nav") {
        li(className = "sidebar-header", content = "Management Tools")

        menuItems.listIterator().forEach {
            when (it.subMenuItem.isEmpty()) {
                true -> {
                    items[it.link?: run { "" }] = Li(className = "sidebar-item") {
                        link("", it.link, className = "sidebar-link", dataNavigo = true) {
                            add(i(className = "align-middle") {
                                setAttribute("data-feather", it.icon)
                            })
                            add(span(className = "align-middle", content = it.title))
                        }
                    }
                    add(items[it.link]!!)
                }
                else -> {
                    val parent = Li(className = "sidebar-item") {
                        link("", null, className = "sidebar-link collapsed") {
                            add(i(className = "align-middle") {
                                setAttribute("data-feather", it.icon)
                            })
                            add(span(className = "align-middle", content = it.title))
                            setAttribute("data-bs-target", "#${it.title}")
                            setAttribute("data-bs-toggle", "collapse")
                        }
                        ul(className = "sidebar-dropdown list-unstyled collapse") {
                            id = it.title
                            setAttribute("data-bs-parent", "#sidebar")

                            it.subMenuItem.listIterator().forEach { sub ->
                                items[sub.link?: run { "" }] = Li(className = "sidebar-item") {
                                    link("", sub.link, className = "sidebar-link", dataNavigo = true) {
                                        add(span(className = "align-middle", content = sub.title))
                                    }
                                }
                                add(items[sub.link]!!)
                            }
                        }
                    }

                    it.subMenuItem.listIterator().forEach { sub ->
                        parents[sub.link?: run { "" }] = parent
                    }
                    add(parent)
                }
            }
        }

        li(className = "sidebar-header", content = "Development Guide")
        link("", "/swagger-ui.html", className = "sidebar-link") {
            target = "_blank"
            add(i(className = "align-middle") {
                setAttribute("data-feather", "book")
            })
            add(span(className = "align-middle", content = "API Documentation"))
        }
    }

    ObservableManager.setSubscriber("menuBar") {
        AppEngine.dashboardPageChangeObserver.subscribe {
            window.setTimeout({
                items.keys.forEach {
                    if(items[it]?.hasCssClass("active") == true) {
                        items[it]?.removeCssClass("active")
                    }
                    if(it == window.location.pathname) {
                        items[it]?.addCssClass("active")
                    }
                }
                parents.keys.forEach {
                    if(parents[it]?.hasCssClass("active") == true) {
                        parents[it]?.removeCssClass("active")
                    }
                }
                parents.keys.forEach {
                    if(it == window.location.pathname) {
                        parents[it]?.getChildren()?.get(0)?.removeCssClass("collapsed")
                        parents[it]?.getChildren()?.get(0)?.setAttribute("aria-expanded", "true")
                        if (parents[it]?.getChildren()?.get(1)?.hasCssClass("show") == false) {
                            parents[it]?.getChildren()?.get(1)?.addCssClass("show")
                        }
                        parents[it]?.addCssClass("active")
                    }
                }
            }, 50)
        }
    }
}

data class MenuItem (
    var title: String?,
    var link: String?,
    var icon: String,
    var subMenuItem: List<MenuItem> = listOf(),
    var external: Boolean = false
)

private const val ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN"

fun getMenu(role: Role): List<MenuItem> {
    val menuItems: MutableList<MenuItem> = mutableListOf()
    when (role.name) {
        ROLE_SUPER_ADMIN -> {
            when(AppEngine.authService.user!!.adminTenantUser && AppEngine.defaultTenantHolder.getTenant() == null) {
                true -> {
                    menuItems.add(MenuItem("Home", "/app/dashboard", "home", listOf()))
                    menuItems.add(MenuItem("Tenants", "", "cloud", listOf(
                        MenuItem("All Tenants", "/app/dashboard/tenants", ""),
                        MenuItem("Add Tenant", "/app/dashboard/tenants/add", ""),
                    )))
                    menuItems.add(MenuItem("Clients", "", "terminal", listOf(
                        MenuItem("All Clients", "/app/dashboard/clients", ""),
                        MenuItem("Add Client", "/app/dashboard/clients/add", "")
                    )))
                    menuItems.add(MenuItem("Users", "", "users", listOf(
                        MenuItem("All Users", "/app/dashboard/users", ""),
                        MenuItem("Add User", "/app/dashboard/users/add", "")
                    )))
                    menuItems.add(MenuItem("Roles", "", "key", listOf(
                        MenuItem("All Roles", "/app/dashboard/roles", ""),
                        MenuItem("Add Role", "/app/dashboard/roles/add", "")
                    )))
                }
                else -> {
                    menuItems.add(MenuItem("Home", "/app/dashboard", "home", listOf()))
                    menuItems.add(MenuItem("Clients", "", "terminal", listOf(
                        MenuItem("All Clients", "/app/dashboard/clients", ""),
                        MenuItem("Add Client", "/app/dashboard/clients/add", "")
                    )))
                    menuItems.add(MenuItem("Users", "", "users", listOf(
                        MenuItem("All Users", "/app/dashboard/users", ""),
                        MenuItem("Add User", "/app/dashboard/users/add", "")
                    )))
                    menuItems.add(MenuItem("Roles", "", "key", listOf(
                        MenuItem("All Roles", "/app/dashboard/roles", ""),
                        MenuItem("Add Role", "/app/dashboard/roles/add", "")
                    )))
                }
            }
        }
        else -> {
            menuItems.add(MenuItem("Home", "/app/dashboard", "home", listOf()))
        }
    }
    return menuItems
}
