package com.bittokazi.oauth2.auth.server.utils

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.Setter
import java.util.function.Function

class CookieActionsProvider() {
    var updateFunction: Function<CookieValue, Void>? = null

    fun updateCookie(cookieValue: CookieValue) {
        updateFunction!!.apply(cookieValue)
    }

    @Getter
    @Setter
    @AllArgsConstructor
    open class CookieValue(
        val key: String,
        val value: String
    )

    companion object {
        fun updateCookieFunc(httpServletResponse: HttpServletResponse): Function<CookieValue, Void> {
            return Function<CookieValue, Void> { cookieValue: CookieValue ->
                val cookie = Cookie(cookieValue.key, cookieValue.value)
                cookie.path = "/"
                cookie.maxAge = 63072000
                httpServletResponse.addCookie(cookie)
                null
            }
        }
    }
}
