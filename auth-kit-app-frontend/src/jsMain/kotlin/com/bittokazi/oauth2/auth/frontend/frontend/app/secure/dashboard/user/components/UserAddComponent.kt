package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.RoleService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.UserService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.UserForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_USER_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.rest.RemoteRequestException
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
fun Container.userAddComponent(): Container {
    val userForm = UserForm(update = false)

    return div {
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
                            window.get("Swal").fire(
                                Json.encodeToDynamic(
                                    mapOf(
                                        "title" to "Success",
                                        "text" to "Added User Successfully.",
                                        "icon" to "success"
                                    )
                                )
                            )
                            AppEngine.routing.navigate(APP_DASHBOARD_USER_ROUTE)
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
            window.get("Swal").fire(
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
