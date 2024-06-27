package com.bittokazi.oauth2.auth.server.utils

import jakarta.servlet.http.HttpServletRequest

object HttpReqRespUtils {

    val logger = logger()

    private val IP_HEADER_CANDIDATES = arrayOf(
        "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
    )

    fun getClientIpAddressIfServletRequestExist(httpServletRequest: HttpServletRequest): String {
        for (header in IP_HEADER_CANDIDATES) {
            val ipList = httpServletRequest.getHeader(header)
            if (ipList != null && ipList.isNullOrEmpty() && !"unknown".equals(ipList, ignoreCase = true)) {
                try {
                    val ip = ipList.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    return ip
                } catch (ex: Exception) {
                    logger.error("Error IP can not be extracted. ", ex.printStackTrace())
                }
            }
        }
        return httpServletRequest.remoteAddr
    }

    fun getUserAgent(httpServletRequest: HttpServletRequest): String {
        return httpServletRequest.getHeader("User-Agent")
    }
}

