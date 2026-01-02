package com.bittokazi.oauth2.auth.server.app.models.master

import com.bittokazi.oauth2.auth.server.config.AppConfig
import java.io.Serializable

data class TenantInfo(
    var cpanel: Boolean = true,
    var enabledConfigPanel: Boolean = true,
    var name: String = "",
    var systemVersion: String = AppConfig.VERSION
) : Serializable
