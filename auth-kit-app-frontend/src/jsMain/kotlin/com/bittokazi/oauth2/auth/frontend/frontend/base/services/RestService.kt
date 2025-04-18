package com.bittokazi.oauth2.auth.frontend.frontend.base.services

import com.bittokazi.oauth2.auth.frontend.frontend.base.models.LoginResponse
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.RefreshTokenRequest
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.defaultAuthHolder
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AuthData
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AuthHolderType
import io.kvision.rest.RemoteRequestException
import io.kvision.rest.RestClient
import io.kvision.rest.RestResponse
import io.kvision.rest.post
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get
import kotlin.js.Promise

class RestService {

    val BASE_URL = window.location.origin
    val API = "${BASE_URL}/api"
    val API_PUBLIC = "${BASE_URL}/public/api"
    val OAUTH_URL = BASE_URL + "/oauth2/login"

    var refreshTokenObservable: ObservableValue<Boolean?> = ObservableValue(null)
    var refreshingToken = false

    fun getClient(): RestClient {
        return RestClient() {
            val headerList = mutableListOf<Pair<String, String>>()

            val token = defaultAuthHolder.getAuth()?.token ?: run { return@run "" }
            headerList.add("Authorization" to "Bearer $token")

            if (!AppEngine.defaultTenantHolder.getTenant().isNullOrEmpty()) {
                headerList.add("X-DATA-TENANT" to AppEngine.defaultTenantHolder.getTenant()!!)
            }
            headers = {
                headerList
            }
        }
    }

    fun getPublicClient(): RestClient {
        return RestClient()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun <T> createAuthCall(req: () -> Promise<RestResponse<T>>): Promise<RestResponse<T>> {
        return Promise { resolve, reject ->
            req().then {
                resolve(it)
            }.catch { throwable ->
                if(throwable is RemoteRequestException) {
                    if (throwable.code.toInt() == 401) {
                        refreshTokenAndRetry(req, resolve, reject)
                    } else if (throwable.code.toInt() == 400) {
                        window.get("Swal").fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Error",
                                    "text" to "Invalid input. Please correct them.",
                                    "icon" to "error"
                                )
                            )
                        )
                        reject(throwable)
                    } else if (throwable.code.toInt() == 404) {
                        window.get("Swal").fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Error",
                                    "text" to "Resource not found [404]",
                                    "icon" to "error"
                                )
                            )
                        )
                    } else if (throwable.code.toInt() == 422) {
                        window.get("Swal").fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Error",
                                    "text" to "Unprocessable request [422]",
                                    "icon" to "error"
                                )
                            )
                        )
                    } else if (throwable.code.toInt() >= 500) {
                        window.get("Swal").fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Error",
                                    "text" to "Server responded with error code [${throwable.code.toInt()}]",
                                    "icon" to "error"
                                )
                            )
                        )
                    } else if (throwable.code.toInt() >= 400) {
                        window.get("Swal").fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Error",
                                    "text" to "Request failed due to client error [${throwable.code.toInt()}]",
                                    "icon" to "error"
                                )
                            )
                        )
                    } else {
                        reject(throwable)
                    }
                } else {
                    reject(throwable)
                }
            }
        }
    }

    fun <T> refreshTokenAndRetry(
        req: (() -> Promise<RestResponse<T>>?)?,
        resolve: ((RestResponse<T>) -> Unit)?,
        reject: ((Throwable) -> Unit)?,
        callback: ((Boolean) -> Unit)? = null
    ) {
        when (refreshingToken) {
            true -> {
                refreshTokenObservable.subscribe {
                    if (it != null) {
                        if (it) {
                            if (req != null) {
                                req()?.then {
                                    resolve?.invoke(it)
                                }
                            }
                        }
                    }
                }
            }
            false -> {
                refreshingToken = true
                refreshTokenObservable = ObservableValue(null)

                val refreshTokenRequest = RefreshTokenRequest(
                    refresh_token = defaultAuthHolder.getAuth()?.refreshToken ?: run { return@run "" }
                )

                RestClient().post<LoginResponse, RefreshTokenRequest>(
                    url = AppEngine.restService.BASE_URL + "/oauth2/refresh/token",
                    data = refreshTokenRequest
                ).then {
                    when (AppEngine.authHolderType) {
                        AuthHolderType.LOCAL_STORAGE -> defaultAuthHolder.setAuth(AuthData(it.access_token, it.refresh_token))
                        AuthHolderType.COOKIE -> {}
                    }

                    refreshTokenObservable.setState(true)

                    if (req != null) {
                        req()?.then {
                            resolve?.invoke(it)
                        }?.catch { throwable ->
                            if (throwable is RemoteRequestException) {
                                if (throwable.code.toInt() == 401) {
                                    AppEngine.authService.logout()
                                } else {
                                    reject?.invoke(throwable)
                                }
                            } else {
                                reject?.invoke(throwable)
                            }
                        }
                    } else {
                        callback?.invoke(true);
                    }
                    refreshingToken = false
                }.catch {
                    refreshingToken = false
                    AppEngine.authService.logout(oauth2LoginPage = true)
                }
            }
        }
    }
}