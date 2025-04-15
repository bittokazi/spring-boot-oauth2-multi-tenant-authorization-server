package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.restService
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Client
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Promise

object ClientService {

    private val clientsBaseUrl = "${restService.API}/clients"

    fun getAll(): Promise<RestResponse<List<Client>>> {
        return restService.createAuthCall {
            restService.getClient().request<List<Client>>(clientsBaseUrl) {
                method = HttpMethod.GET
            }
        }
    }

    fun get(id: String): Promise<RestResponse<Client>> {
        return restService.createAuthCall {
            restService.getClient().request<Client>("$clientsBaseUrl/$id") {
                method = HttpMethod.GET
            }
        }
    }

    fun delete(id: String): Promise<RestResponse<Client>> {
        return restService.createAuthCall {
            restService.getClient().request<Client>("$clientsBaseUrl/$id") {
                method = HttpMethod.DELETE
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun create(client: Client): Promise<RestResponse<Client>> {
        return return restService.createAuthCall {
            restService.getClient().request<Client>(clientsBaseUrl) {
                method = HttpMethod.POST
                data =  Json.encodeToDynamic(client)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun update(client: Client): Promise<RestResponse<Client>> {
        return return restService.createAuthCall {
            restService.getClient().request<Client>("$clientsBaseUrl/${client.id}") {
                method = HttpMethod.PUT
                data =  Json.encodeToDynamic(client)
            }
        }
    }
}
