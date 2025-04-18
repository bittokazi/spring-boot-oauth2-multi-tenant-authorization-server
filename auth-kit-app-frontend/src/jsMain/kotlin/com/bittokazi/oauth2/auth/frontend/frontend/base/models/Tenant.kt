package com.bittokazi.oauth2.auth.frontend.frontend.base.models

import kotlinx.serialization.Serializable

@Serializable
data class Tenant (
    val id: String? = null,
    var companyKey: String? = null,
    var enabled: Boolean = false,
    var name: String? = null,
    var domain: String? = null,
    var signInBtnColor: String? = null,
    var resetPasswordLink: String? = null,
    var createAccountLink: String? = null,
    var defaultRedirectUrl: String? = null,
    var enableConfigPanel: Boolean? = null,
    var enableCustomTemplate: Boolean? = null,
    var customTemplateLocation: String? = null
)
