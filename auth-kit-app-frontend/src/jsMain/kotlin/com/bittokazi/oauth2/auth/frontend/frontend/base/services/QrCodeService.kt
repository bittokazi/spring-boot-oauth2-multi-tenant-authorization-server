package com.bittokazi.oauth2.auth.frontend.frontend.base.services

import com.bittokazi.oauth2.auth.frontend.frontend.base.models.TwoFASecretPayload
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi

object QrCodeService {

    @OptIn(ExperimentalSerializationApi::class)
    fun create(twoFASecretPayload: TwoFASecretPayload, id: String = "", username: String? = "", fn: ()-> dynamic) {
        fn().clear()
        window.document.getElementById(id)?.innerHTML = ""
        fn().makeCode( "otpauth://totp/${window.location.protocol}//${window.location.host}" +
                ":${username}?secret=${twoFASecretPayload.secret}")
    }
}
