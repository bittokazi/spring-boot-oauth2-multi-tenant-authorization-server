package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.RoleService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.UserService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.UserForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.UserPasswordUpdateForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userPasswordFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userPasswordFormErrorHandler
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
fun Container.userUpdateComponent(id: String): Container {
    val userForm = UserForm(update = true)
    var userPasswordUpdateForm = UserPasswordUpdateForm(self = false)

    return div {
        RoleService.getAll().then { roleList ->
            UserService.get(id).then { userResponse ->
                userFormComponent(
                    userForm = userForm,
                    user = userResponse.data,
                    rolesList = roleList.data.roles,
                    update = true
                ) {
                    when (userForm.isValid()) {
                        true -> {
                            UserService.update(
                                User(
                                    id = id,
                                    firstName = userForm.firstName.getValue(),
                                    lastName = userForm.lastName.getValue(),
                                    username = userForm.username.getValue(),
                                    email = userForm.email.getValue(),
                                    roles = listOf(
                                        Role(
                                            id = userForm.roles.getValue(),
                                        )
                                    )
                                )
                            ).then {
                                window.get("Swal").fire(
                                    Json.encodeToDynamic(
                                        mapOf(
                                            "title" to "Success",
                                            "text" to "Updated User Successfully.",
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
                                userFormErrorHandler(null, userForm)
                            }
                        }
                        false -> {

                        }
                    }
                }

                userPasswordFormComponent(
                    self = false,
                    userPasswordUpdateForm = userPasswordUpdateForm
                ) {
                    when (userPasswordUpdateForm.isValid()) {
                        true -> {
                            UserService.updatePassword(
                                User(
                                    id = id,
                                    newPassword = userPasswordUpdateForm.newPassword.getValue(),
                                )
                            ).then {
                                window.get("Swal").fire(
                                    Json.encodeToDynamic(
                                        mapOf(
                                            "title" to "Success",
                                            "text" to "Updated User Password Successfully.",
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
                                            return@then userPasswordFormErrorHandler(null, userPasswordUpdateForm)
                                        }
                                    }
                                }
                                userPasswordFormErrorHandler(null, userPasswordUpdateForm)
                            }
                        }
                        false -> {

                        }
                    }
                }
            }.catch {
                window["Swal"].fire(
                    Json.encodeToDynamic(
                        mapOf(
                            "title" to "Error",
                            "text" to "Error while fetching user",
                            "icon" to "error"
                        )
                    )
                )
            }
        }.catch {
            window["Swal"].fire(
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
