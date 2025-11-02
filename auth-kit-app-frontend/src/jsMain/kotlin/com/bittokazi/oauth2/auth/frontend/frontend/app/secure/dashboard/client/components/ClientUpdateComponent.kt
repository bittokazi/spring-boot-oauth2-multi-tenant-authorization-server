package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.ClientService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form.ClientForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form.clientFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form.clientFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_CLIENT_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Client
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
class ClientUpdateComponent (
    id: String
): SimplePanel() {
    val clientForm = ClientForm(update = true)

    init {
        div {
            ClientService.get(id).then { client ->
                clientFormComponent(
                    clientForm = clientForm,
                    client = client.data,
                    update = true
                ) {
                    when (clientForm.isValid()) {
                        true -> {
                            ClientService.update(
                                Client(
                                    id = id,
                                    clientId = clientForm.clientId.getValue(),
                                    resourceIds = clientForm.resourceIds.getValue(),
                                    scope = clientForm.scope.getValue()?.split(",")?.map { it.trim() },
                                    clientAuthenticationMethod = clientForm.clientAuthenticationMethod.getValue(),
                                    authorizedGrantTypes = clientForm.authorizedGrantTypes.getValue()?.split(",")
                                        ?.map { it.trim() },
                                    webServerRedirectUri = clientForm.webServerRedirectUri.getValue()?.split(",")
                                        ?.map { it.trim() },
                                    authorities = client.data.authorities,
                                    accessTokenValidity = clientForm.accessTokenValidity.getValue()?.toIntOrNull(),
                                    refreshTokenValidity = clientForm.refreshTokenValidity.getValue()?.toIntOrNull(),
                                    additionalInformation = client.data.additionalInformation,
                                    requireConsent = clientForm.requireConsent.getValue(),
                                    postLogoutUrl = clientForm.postLogoutUrl.getValue(),
                                    tokenType = clientForm.tokenType.getValue(),
                                    generateSecret = clientForm.generateSecret.getValue()
                                )
                            ).then { updateResponse ->
                                when (updateResponse.data.newSecret) {
                                    null -> {
                                        sweetAlert.fire(
                                            Json.encodeToDynamic(
                                                mapOf(
                                                    "title" to "Success",
                                                    "text" to "Updated Client with ID [$id]",
                                                    "icon" to "success"
                                                )
                                            )
                                        )
                                    }
                                    else -> {
                                        sweetAlert.fire(
                                            Json.encodeToDynamic(
                                                mapOf(
                                                    "title" to "Success",
                                                    "type" to "warning",
                                                    "confirmButtonColor" to "#3085d6",
                                                    "cancelButtonColor" to "#d33",
                                                    "confirmButtonText" to "Dismiss",
                                                    "allowOutsideClick" to null,
                                                    "html" to "<p style=\"text-align: justify\">" +
                                                            "New Secret Generated<br />" +
                                                            "<span style=\"font-weight: bold;\">" +
                                                            "ID:</span>&nbsp;${updateResponse.data.clientId}<br />" +
                                                            "<span style=\"font-weight: bold;\">" +
                                                            "Secret:</span>&nbsp;${updateResponse.data.newSecret}</p>"
                                                )
                                            )
                                        )
                                    }
                                }
                                SpaAppEngine.routing.navigate(APP_DASHBOARD_CLIENT_ROUTE)
                            }.catch { throwable ->
                                clientFormErrorHandler(null, clientForm)
                            }
                        }
                        false -> {

                        }
                    }
                }
            }.catch {

            }
        }
    }
}
