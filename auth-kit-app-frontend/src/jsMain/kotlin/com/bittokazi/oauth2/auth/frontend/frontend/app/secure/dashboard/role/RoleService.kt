package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.restService
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.RoleList
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Promise

object RoleService {

    fun getAll(): Promise<RestResponse<RoleList>> {
        return restService.createAuthCall {
            restService.getClient().request<RoleList>("${restService.API}/roles/all") {
                method = HttpMethod.GET
            }
        }
    }

    fun get(id: String): Promise<RestResponse<Role>> {
        return restService.createAuthCall {
            restService.getClient().request<Role>("${restService.API}/roles/$id") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun create(role: Role): Promise<RestResponse<Role>> {
        return restService.createAuthCall {
            restService.getClient().request<Role>("${restService.API}/roles") {
                method = HttpMethod.POST
                data = Json.encodeToDynamic(role)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun update(role: Role): Promise<RestResponse<Role>> {
        return restService.createAuthCall {
            restService.getClient().request<Role>("${restService.API}/roles/${role.id}") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(role)
            }
        }
    }
}
