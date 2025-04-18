package com.bittokazi.oauth2.auth.frontend.frontend.base.common

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DefaultAuthHolder : AuthHolder {
    override fun setAuth(authData: AuthData?) {
        localStorage.setItem("auth", Json.encodeToString(authData))
    }

    override fun getAuth(): AuthData? {
        localStorage.getItem("auth")?.let {
            return Json.decodeFromString(it)
        }
        return null
    }
}

class AuthInformationHolder : AuthHolder, TenantHolder {
    override fun setAuth(authData: AuthData?) {}

    override fun getAuth(): AuthData? {
        val accessToken = getCookie("access_token")
        val refreshToken = getCookie("refresh_token")
        return when (accessToken != null && refreshToken !=null) {
            true -> AuthData(
                token = accessToken,
                refreshToken = refreshToken
            )

            false -> null
        }
    }

    fun getCookie(name: String): String? {
        val regex = Regex("(?:^|;\\s*)$name=([^;]*)") // Matches "brown " followed by one or more word characters

        val matchResult = regex.find(window.document.cookie)
        if (matchResult != null) {
            return matchResult.groupValues[1]
        } else {
            return null
        }
    }

    override fun getTenant(): String? {
        localStorage.getItem("tenant")?.let {
            if(it.isEmpty() || it.isBlank()) return null
            return it
        }
        return null
    }

    override fun setTenant(tenant: String?) {
        if (tenant == null) {
            localStorage.setItem("tenant", "")
        } else {
            localStorage.setItem("tenant", tenant)
        }
    }
}

interface AuthHolder {
    fun setAuth(authData: AuthData?)
    fun getAuth(): AuthData?
}

interface TenantHolder {
    fun getTenant(): String?
    fun setTenant(tenant: String?)
}

enum class AuthHolderType {
    LOCAL_STORAGE, COOKIE
}

@Serializable
data class AuthData(
    var token: String,
    val refreshToken: String
)
