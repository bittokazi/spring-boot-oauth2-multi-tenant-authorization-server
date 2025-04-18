package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.roleAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.roleListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.roleUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ROLE_ADD_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ROLE_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ROLE_UPDATE_ROUTE
import io.kvision.core.Container

fun Container.roleModule(layoutContainer: Container) {
    AppEngine.routing
        .on(APP_DASHBOARD_ROLE_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(roleListComponent())
                AppEngine.pageTitleObserver.setState("All Roles")
            }
        })
        .on(APP_DASHBOARD_ROLE_ADD_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(roleAddComponent())
                AppEngine.pageTitleObserver.setState("Add Role")
            }
        })
        .on(APP_DASHBOARD_ROLE_UPDATE_ROUTE(":id"), {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(roleUpdateComponent(it.data.id))
                AppEngine.pageTitleObserver.setState("Update Role")
            }
        })
}
