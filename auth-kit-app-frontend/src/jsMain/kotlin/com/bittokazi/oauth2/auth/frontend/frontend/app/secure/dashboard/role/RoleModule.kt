package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role

import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredPageModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.RoleAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.RoleListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.RoleUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ROLE_ADD_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ROLE_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ROLE_UPDATE_ROUTE

fun roleModule() = DefaultSecuredPageModule(
    RouterConfiguration(
        route = APP_DASHBOARD_ROLE_ROUTE,
        title = "All Roles",
        view = {
            RoleListComponent()
        }
    ),
    RouterConfiguration(
        route = APP_DASHBOARD_ROLE_ADD_ROUTE,
        title = "Add Role",
        view = {
            RoleAddComponent()
        }
    ),
    RouterConfiguration(
        route = APP_DASHBOARD_ROLE_UPDATE_ROUTE(":id"),
        title = "Update Role",
        view = {
            RoleUpdateComponent(it.data.id)
        }
    )
)
