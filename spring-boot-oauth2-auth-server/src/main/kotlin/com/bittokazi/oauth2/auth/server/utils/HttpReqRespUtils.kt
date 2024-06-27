package com.bittokazi.oauth2.auth.server.utils

import jakarta.servlet.http.HttpServletRequest

object HttpReqRespUtils {
    private val IP_HEADER_CANDIDATES = arrayOf(
        "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
    )

    fun getClientIpAddressIfServletRequestExist(httpServletRequest: HttpServletRequest): String {
        for (header in IP_HEADER_CANDIDATES) {
            val ipList = httpServletRequest.getHeader(header)
            if (ipList.isNullOrEmpty() && !"unknown".equals(ipList, ignoreCase = true)) {
                val ip = ipList.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                return ip
            }
        }
        return httpServletRequest.remoteAddr
    }

    fun getUserAgent(httpServletRequest: HttpServletRequest): String {
        return httpServletRequest.getHeader("User-Agent")
    }
}

