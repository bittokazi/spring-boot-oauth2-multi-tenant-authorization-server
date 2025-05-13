package com.bittokazi.oauth2.auth.server.utils

import eu.bitwalker.useragentutils.UserAgent
import jakarta.servlet.http.HttpServletRequest

object HttpReqRespUtils {

    val logger = logger()

    private val IP_HEADER_CANDIDATES = arrayOf(
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    )

    fun getClientIpAddressIfServletRequestExist(httpServletRequest: HttpServletRequest): String {
        IP_HEADER_CANDIDATES.forEach { header ->
            val ipList = httpServletRequest.getHeader(header)
            if (ipList != null && ipList.isNotEmpty() && !"unknown".equals(ipList, ignoreCase = true)) {
                try {
                    val ips = ipList.split(",").map { it.trim() }
                    return ips.firstOrNull() ?: httpServletRequest.remoteAddr
                } catch (ex: Exception) {
                    logger.error("Error extracting IP address from header $header: ", ex)
                }
            }
        }
        return httpServletRequest.remoteAddr
    }

    fun getUserAgent(httpServletRequest: HttpServletRequest): String {
        val userAgentString = httpServletRequest.getHeader("User-Agent")
        return when (userAgentString.isNotEmpty()) {
            true -> {
                val userAgent = UserAgent.parseUserAgentString(userAgentString)
                val os = userAgent.operatingSystem
                val browser = userAgent.browser

                "OS: ${os.name}, Browser: ${browser.name}"
            }
            false -> "Unknown User-Agent"
        }
    }
}
