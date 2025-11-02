package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.RoleService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.form.RoleForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.form.roleFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.form.roleFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ROLE_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.rest.RemoteRequestException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
class RoleUpdateComponent(
    id: String
): SimplePanel() {

    val roleForm = RoleForm()

    init {
        div {
            RoleService.get(id).then { role ->
                roleFormComponent(
                    update = true,
                    roleForm = roleForm,
                    role = role.data
                ) {
                    if (roleForm.isValid()) {
                        roleForm.submitButton.showLoading()
                        RoleService.update(
                            Role(
                                id = id,
                                name = roleForm.name.input.value.orEmpty(),
                                title = roleForm.title.input.value.orEmpty(),
                                description = ""
                            )
                        ).then {
                            sweetAlert.fire(
                                Json.encodeToDynamic(
                                    mapOf(
                                        "title" to "Success",
                                        "text" to "Updated Role Successfully.",
                                        "icon" to "success"
                                    )
                                )
                            )
                            SpaAppEngine.routing.navigate(APP_DASHBOARD_ROLE_ROUTE)
                        }.catch { throwable ->
                            if (throwable is RemoteRequestException) {
                                if (throwable.code.toInt() == 400) {
                                    throwable.response?.text()?.then {
                                        val response: Map<String, List<String>> = Json.decodeFromString(it)
                                        return@then roleFormErrorHandler(response, roleForm)
                                    }
                                }
                            }
                            roleFormErrorHandler(null, roleForm)
                        }
                    }
                }
            }
        }
    }
}
