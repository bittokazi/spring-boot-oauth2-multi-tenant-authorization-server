package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant

import com.bittokazi.kvision.spa.framework.base.common.tenant.TenantInformationProvider
import com.bittokazi.kvision.spa.framework.base.models.SpaTenantInfo
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Tenant
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.TenantInfo
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.restService
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Promise

class TenantService: TenantInformationProvider {

    val roleBaseUrl = "${restService.BASE_URL}/api/tenants"

    fun getCompanyInfo(): Promise<RestResponse<TenantInfo>> {
        return restService.getPublicClient().request<TenantInfo>("${restService.BASE_URL}/public/api/tenants/info") {
            method = HttpMethod.GET
        }
    }

    fun getAll(): Promise<RestResponse<List<Tenant>>> {
        return restService.createAuthCall {
            restService.getClient().request<List<Tenant>>(roleBaseUrl) {
                method = HttpMethod.GET
            }
        }
    }

    fun get(id: String): Promise<RestResponse<Tenant>> {
        return restService.createAuthCall {
            restService.getClient().request<Tenant>("$roleBaseUrl/$id") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun create(tenant: Tenant): Promise<RestResponse<Tenant>> {
        return restService.createAuthCall {
            restService.getClient().request<Tenant>(roleBaseUrl) {
                method = HttpMethod.POST
                data = Json.encodeToDynamic(tenant)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun update(tenant: Tenant): Promise<RestResponse<Tenant>> {
        return restService.createAuthCall {
            restService.getClient().request<Tenant>("$roleBaseUrl/${tenant.id}") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(tenant)
            }
        }
    }

    override fun getTenantInfoProvider(): Promise<SpaTenantInfo> {
        return Promise { resolve, reject ->
            getCompanyInfo().then {
                resolve(
                    SpaTenantInfo(
                        cpanel = it.data.cpanel,
                        enabledConfigPanel = it.data.enabledConfigPanel,
                        name = it.data.name,
                        systemVersion = it.data.systemVersion
                    )
                )
            }.catch { throwable ->
                console.log(throwable)
                reject(throwable)
            }
        }
    }
}
