package com.bittokazi.oauth2.auth.frontend.frontend.base.services

import com.bittokazi.kvision.spa.framework.base.common.AuthData
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.common.AuthInformationProvider
import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.defaultAuthHolder
import com.bittokazi.kvision.spa.framework.base.models.RefreshTokenRequestProvider
import com.bittokazi.kvision.spa.framework.base.models.SpaRole
import com.bittokazi.kvision.spa.framework.base.models.SpaUser
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.rest.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.Promise

class AuthService: AuthInformationProvider, RefreshTokenRequestProvider {

    var user: User? = null
    private val restService = SpaAppEngine.restService

    fun whoAmI(): Promise<RestResponse<User>> {
        return SpaAppEngine.restService.createAuthCall {
            restService.getClient().request<User>("${restService.BASE_URL}/api/users/whoami") {
                method = HttpMethod.GET
            }
        }
    }

    override fun getAuthProvider(): Promise<SpaUser> {
        return Promise { resolve, reject ->
            whoAmI().then {
                user = it.data
                resolve(
                    SpaUser(
                        id = it.data.id,
                        email = it.data.email,
                        firstName = it.data.firstName,
                        lastName = it.data.lastName,
                        avatarImage = it.data.avatarImage,
                        spaRoles = it.data.roles?.map { role ->
                            SpaRole(
                                title = role.title,
                                name = role.name,
                                id = role.id
                            )
                        },
                        adminTenantUser = it.data.adminTenantUser
                    )
                )
            }.catch { throwable ->
                reject(throwable)
            }
        }
    }

    override fun getRequest(): JsonObject {
        return JsonObject(
            mapOf(
                "refresh_token" to JsonPrimitive(defaultAuthHolder.getAuth()?.refreshToken ?: "")
            )
        )
    }

    override fun getAuthDataFromRefreshTokenResponse(response: JsonObject): AuthData {
        return AuthData(
            token = response["access_token"]?.jsonPrimitive?.content ?: "",
            refreshToken = response["refresh_token"]?.jsonPrimitive?.content ?: ""
        )
    }
}
