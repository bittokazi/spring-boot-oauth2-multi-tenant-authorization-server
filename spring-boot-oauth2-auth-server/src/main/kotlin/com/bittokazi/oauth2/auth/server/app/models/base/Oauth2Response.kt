package com.bittokazi.oauth2.auth.server.app.models.base

import lombok.Getter
import lombok.Setter

@Getter
@Setter
class Oauth2Response {
    lateinit var access_token: String

    lateinit var refresh_token: String

    lateinit var token_type: String

    var expires_in: Long = 0
}
