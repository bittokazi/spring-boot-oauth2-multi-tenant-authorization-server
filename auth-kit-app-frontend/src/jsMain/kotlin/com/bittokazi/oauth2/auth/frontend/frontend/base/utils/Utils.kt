package com.bittokazi.oauth2.auth.frontend.frontend.base.utils

import com.bittokazi.oauth2.auth.frontend.frontend.app.moment

object Utils {

    fun formatTimeFromNow(dateTime: String?): String? {
        return dateTime?.let { moment(dateTime).fromNow() } ?: run { "" }
    }
}
