package com.bittokazi.oauth2.auth.frontend.frontend.base.menu

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.components.menu.MenuItem
import com.bittokazi.kvision.spa.framework.base.components.menu.MenuProvider
import com.bittokazi.kvision.spa.framework.base.components.menu.MenuSection
import com.bittokazi.kvision.spa.framework.base.models.SpaRole

class DefaultMenuProvider: MenuProvider {
    override fun getMenu(spaRole: SpaRole): List<MenuSection> {
        val menuSections: MutableList<MenuSection> = mutableListOf()
        val menuItems: MutableList<MenuItem> = mutableListOf()

        when (spaRole.name) {
            ROLE_SUPER_ADMIN -> {
                when(SpaAppEngine.spaAuthService.spaUser!!.adminTenantUser && SpaAppEngine.defaultTenantHolder.getTenant() == null) {
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

        menuSections.add(
            MenuSection(
                title = "Management Tools",
                link = "",
                icon = "",
                menuItems = menuItems
            )
        )
        menuSections.add(
            MenuSection(
                title = "Development Guid",
                link = "",
                icon = "",
                menuItems = listOf(
                    MenuItem(
                        title = "API Documentation",
                        link = "/swagger-ui.html",
                        icon = "book",
                        subMenuItems = listOf(),
                        external = true
                    )
                )
            )
        )
        return menuSections
    }
}

private const val ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN"
