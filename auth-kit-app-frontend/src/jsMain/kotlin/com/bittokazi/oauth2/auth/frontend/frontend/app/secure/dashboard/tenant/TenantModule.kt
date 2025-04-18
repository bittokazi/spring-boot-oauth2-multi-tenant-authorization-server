package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.tenantAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.tenantListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.tenantUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_ADD_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_UPDATE_ROUTE
import io.kvision.core.Container

fun Container.tenantModule(layoutContainer: Container) {
    AppEngine.routing
        .on(APP_DASHBOARD_TENANT_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(tenantListComponent())
                AppEngine.pageTitleObserver.setState("All Tenants")
            }
        })
        .on(APP_DASHBOARD_TENANT_ADD_ROUTE, {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(tenantAddComponent())
                AppEngine.pageTitleObserver.setState("Add Tenant")
            }
        })
        .on(APP_DASHBOARD_TENANT_UPDATE_ROUTE(":id"), {
            AppEngine.authService.authenticated {
                layoutContainer.removeAll()
                layoutContainer.add(tenantUpdateComponent(it.data.id))
                AppEngine.pageTitleObserver.setState("Update Tenant")
            }
        })
}
