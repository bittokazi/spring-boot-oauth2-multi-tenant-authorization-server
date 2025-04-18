package com.bittokazi.oauth2.auth.frontend.frontend.app.public

import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import io.kvision.html.p
import io.kvision.panel.SimplePanel

class HomePage: SimplePanel() {
    init {
        AppEngine.routing.navigate(AppEngine.APP_DASHBOARD_ROUTE)
        p {
            content = "Redirecting..."
        }
    }
}
