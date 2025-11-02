package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.restService
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.UserList
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.TwoFASecretPayload
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.UserTrustedDevice
import io.kvision.rest.HttpMethod
import io.kvision.rest.RestResponse
import io.kvision.rest.request
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Promise

object UserService {

    private val userBaseUrl = "${restService.BASE_URL}/api/users"

    fun getAll(page: Int = 1, count: Int = 10): Promise<RestResponse<UserList>> {
        return restService.createAuthCall {
            restService.getClient().request<UserList>("$userBaseUrl?page=$page&count=$count") {
                method = HttpMethod.GET
            }
        }
    }

    fun get(id: String): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/$id") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun create(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>(userBaseUrl) {
                method = HttpMethod.POST
                data =  Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun update(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/${user.id}") {
                method = HttpMethod.PUT
                data =  Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updatePassword(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/${user.id}/update/password") {
                method = HttpMethod.PUT
                data =  Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updateMyProfile(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/whoami") {
                method = HttpMethod.PUT
                data =  Json.encodeToDynamic(user)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updateMyPassword(user: User): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/whoami/password") {
                method = HttpMethod.PUT
                data = Json.encodeToDynamic(user)
            }
        }
    }

    fun generateSecret(): Promise<RestResponse<TwoFASecretPayload>> {
        return restService.createAuthCall {
            restService.getClient().request<TwoFASecretPayload>("$userBaseUrl/whoami/mfa/otp/generate-secret") {
                method = HttpMethod.GET
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun enableTwoFa(twoFASecretPayload: TwoFASecretPayload): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/whoami/mfa/otp/enable") {
                method = HttpMethod.POST
                data = Json.encodeToDynamic(twoFASecretPayload)
            }
        }
    }

    fun disableTwoFa(): Promise<RestResponse<User>> {
        return restService.createAuthCall {
            restService.getClient().request<User>("$userBaseUrl/whoami/mfa/otp/disable") {
                method = HttpMethod.GET
            }
        }
    }

    fun reGenerateScratchCodes(): Promise<RestResponse<List<String>>> {
        return restService.createAuthCall {
            restService.getClient().request<List<String>>("$userBaseUrl/whoami/mfa/generate-scratch-codes") {
                method = HttpMethod.GET
            }
        }
    }

    fun getTrustedDevices(): Promise<RestResponse<List<UserTrustedDevice>>> {
        return restService.createAuthCall {
            restService.getClient().request<List<UserTrustedDevice>>("$userBaseUrl/whoami/mfa/trusted-devices") {
                method = HttpMethod.GET
            }
        }
    }

    fun deleteTrustedDevice(id: Long): Promise<RestResponse<UserTrustedDevice>> {
        return restService.createAuthCall {
            restService.getClient().request<UserTrustedDevice>("$userBaseUrl/whoami/mfa/trusted-devices/$id") {
                method = HttpMethod.DELETE
            }
        }
    }
}
