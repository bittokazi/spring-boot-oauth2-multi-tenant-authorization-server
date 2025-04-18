package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.userAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.userListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.userUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_USER_ADD_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_USER_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_USER_UPDATE_ROUTE
import io.kvision.core.Container

fun Container.userModule(layoutContainer: Container) {
    AppEngine.routing
        .on(APP_DASHBOARD_USER_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(userListComponent())
                AppEngine.pageTitleObserver.setState("All Users")
            }
        })
        .on(APP_DASHBOARD_USER_ADD_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(userAddComponent())
                AppEngine.pageTitleObserver.setState("Add User")
            }
        })
        .on(APP_DASHBOARD_USER_UPDATE_ROUTE(":id"), {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(userUpdateComponent(it.data.id))
                AppEngine.pageTitleObserver.setState("Update User")
            }
        })
}

