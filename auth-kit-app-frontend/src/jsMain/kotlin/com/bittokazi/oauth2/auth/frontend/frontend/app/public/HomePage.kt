package com.bittokazi.oauth2.auth.frontend.frontend.app.public

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import io.kvision.html.p
import io.kvision.panel.SimplePanel

class HomePage: SimplePanel() {
    init {
        SpaAppEngine.routing.navigate(AppEngine.APP_DASHBOARD_ROUTE)
        p {
            content = "Redirecting..."
        }
    }
}
