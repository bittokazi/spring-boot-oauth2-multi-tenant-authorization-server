package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.RoleService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.UserService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.UserForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_USER_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.rest.RemoteRequestException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
class UserAddComponent: SimplePanel() {

    val userForm = UserForm(update = false)

    init {
        div {
            RoleService.getAll().then { roleList ->
                userFormComponent(
                    userForm = userForm,
                    user = null,
                    rolesList = roleList.data.roles
                ) {
                    when (userForm.isValid()) {
                        true -> {
                            UserService.create(
                                User(
                                    firstName = userForm.firstName.getValue(),
                                    lastName = userForm.lastName.getValue(),
                                    username = userForm.username.getValue(),
                                    email = userForm.email.getValue(),
                                    roles = listOf(
                                        Role(
                                            id = userForm.roles.getValue(),
                                        )
                                    ),
                                    newPassword = userForm.newPassword.getValue()
                                )
                            ).then {
                                sweetAlert.fire(
                                    Json.encodeToDynamic(
                                        mapOf(
                                            "title" to "Success",
                                            "text" to "Added User Successfully.",
                                            "icon" to "success"
                                        )
                                    )
                                )
                                SpaAppEngine.routing.navigate(APP_DASHBOARD_USER_ROUTE)
                            }.catch { throwable ->
                                if (throwable is RemoteRequestException) {
                                    if (throwable.code.toInt() == 400) {
                                        throwable.response?.text()?.then {
                                            val response: Map<String, List<String>> = Json.decodeFromString(it)
                                            return@then userFormErrorHandler(response, userForm)
                                        }
                                    }
                                }
                                console.log(throwable)
                                userFormErrorHandler(null, userForm)
                            }
                        }
                        false -> {

                        }
                    }
                }
            }.catch {
                sweetAlert.fire(
                    Json.encodeToDynamic(
                        mapOf(
                            "title" to "Error",
                            "text" to "Error while fetching roles",
                            "icon" to "error"
                        )
                    )
                )
            }
        }
    }
}
