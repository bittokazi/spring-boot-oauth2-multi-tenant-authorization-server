package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant

import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Tenant
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.TenantInfo
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.restService
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import io.kvision.state.ObservableValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Promise

object TenantService {

    lateinit var tenantInfo: TenantInfo
    val tenantInfoObserver: ObservableValue<TenantInfo?> = ObservableValue(null)

    fun getCompanyInfo(): Promise<RestResponse<TenantInfo>> {
        return restService.getPublicClient().request<TenantInfo>("${restService.API_PUBLIC}/tenants/info") {
            method = HttpMethod.GET
        }
    }

    fun getAll(): Promise<RestResponse<List<Tenant>>> {
        return restService.createAuthCall {
            restService.getClient().request<List<Tenant>>("${restService.API}/tenants") {
                method = HttpMethod.GET
            }
        }
    }

    fun get(id: String): Promise<RestResponse<Tenant>> {
        return restService.createAuthCall {
            restService.getClient().request<Tenant>("${restService.API}/tenants/$id") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun create(tenant: Tenant): Promise<RestResponse<Tenant>> {
        return restService.createAuthCall {
            restService.getClient().request<Tenant>("${restService.API}/tenants") {
                method = HttpMethod.POST
                data = Json.encodeToDynamic(tenant)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun update(tenant: Tenant): Promise<RestResponse<Tenant>> {
        return restService.createAuthCall {
            restService.getClient().request<Tenant>("${restService.API}/tenants/${tenant.id}") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(tenant)
            }
        }
    }
}
