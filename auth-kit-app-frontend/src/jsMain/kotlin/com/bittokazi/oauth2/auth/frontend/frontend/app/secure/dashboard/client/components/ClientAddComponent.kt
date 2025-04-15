package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.ClientService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form.ClientForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form.clientFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form.clientFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_CLIENT_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Client
import io.kvision.core.Container
import io.kvision.html.div
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
fun Container.clientAddComponent(): Container {
    val clientForm = ClientForm(update = false)

    return div {
        clientFormComponent(
            clientForm = clientForm,
            client = null
        ) {
            when (clientForm.isValid()) {
                true -> {
                    ClientService.create(
                        Client(
                            clientId = clientForm.clientId.getValue(),
                            resourceIds = clientForm.resourceIds.getValue(),
                            scope = clientForm.scope.getValue()?.split(",")?.map { it.trim() },
                            clientAuthenticationMethod = clientForm.clientAuthenticationMethod.getValue(),
                            authorizedGrantTypes = clientForm.authorizedGrantTypes.getValue()?.split(",")
                                ?.map { it.trim() },
                            webServerRedirectUri = clientForm.webServerRedirectUri.getValue()?.split(",")
                                ?.map { it.trim() },
                            authorities = "",
                            accessTokenValidity = clientForm.accessTokenValidity.getValue()?.toIntOrNull(),
                            refreshTokenValidity = clientForm.refreshTokenValidity.getValue()?.toIntOrNull(),
                            additionalInformation = "",
                            requireConsent = clientForm.requireConsent.getValue(),
                            postLogoutUrl = clientForm.postLogoutUrl.getValue(),
                            tokenType = clientForm.tokenType.getValue(),
                            generateSecret = clientForm.generateSecret.getValue()
                        )
                    ).then {
                        window.get("Swal").fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Success",
                                    "type" to "warning",
                                    "confirmButtonColor" to "#3085d6",
                                    "cancelButtonColor" to "#d33",
                                    "confirmButtonText" to "Dismiss",
                                    "allowOutsideClick" to null,
                                    "html" to "<p style=\"text-align: justify\">" +
                                            "New Client Id and Secret Generated<br />" +
                                            "<span style=\"font-weight: bold;\">" +
                                            "ID:</span>&nbsp;${it.data.clientId}<br />" +
                                            "<span style=\"font-weight: bold;\">" +
                                            "Secret:</span>&nbsp;${it.data.newSecret}</p>"
                                )
                            )
                        )
                        AppEngine.routing.navigate(APP_DASHBOARD_CLIENT_ROUTE)
                    }.catch { throwable ->
                        clientFormErrorHandler(null, clientForm)
                    }
                }
                false -> {

                }
            }
        }
    }
}
