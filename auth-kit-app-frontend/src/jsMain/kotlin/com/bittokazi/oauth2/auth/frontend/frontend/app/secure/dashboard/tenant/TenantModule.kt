package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant

import com.bittokazi.kvision.spa.framework.base.common.RouterConfiguration
import com.bittokazi.kvision.spa.framework.base.common.module.DefaultSecuredPageModule
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.TenantAddComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.TenantListComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.TenantUpdateComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_ADD_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_UPDATE_ROUTE

fun tenantModule() = DefaultSecuredPageModule(
    RouterConfiguration(
        route = APP_DASHBOARD_TENANT_ROUTE,
        title = "All Tenants",
        view = {
            TenantListComponent()
        }
    ),
    RouterConfiguration(
        route = APP_DASHBOARD_TENANT_ADD_ROUTE,
        title = "Add Tenant",
        view = {
            TenantAddComponent()
        }
    ),
    RouterConfiguration(
        route = APP_DASHBOARD_TENANT_UPDATE_ROUTE(":id"),
        title = "Update Tenant",
        view = {
            TenantUpdateComponent(it.data.id)
        }
    )
)
