package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.account.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.UserService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.UserForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.html.nav
import io.kvision.html.span
import io.kvision.rest.RemoteRequestException
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
fun Container.myProfileComponent(): Container {
    val userForm = UserForm(update = true, self = true)
    var user: User

    return div {
        div(className = "row mt-3") {
            nav(className = "nav nav-pills nav-justified") {
                link(
                    label = "",
                    url = APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE,
                    className = "nav-link active",
                    dataNavigo = true
                ) {
                    span(className = "feather-sm me-1") {
                        setAttribute("data-feather", "user")
                    }
                    +" My Profile"
                }
                link(
                    label = "",
                    url = APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE,
                    className = "nav-link",
                    dataNavigo = true
                ) {
                    span(className = "feather-sm me-1") {
                        setAttribute("data-feather", "lock")
                    }
                    +" Security"
                }
            }
        }
        div(className = "row mt-4") {
            div(className = "card mb-12") {
                div(className = "card-body") {
                    div {
                        AppEngine.authService.whoAmI().then { userResponse ->
                            user = userResponse.data
                            userFormComponent(
                                userForm = userForm,
                                user = userResponse.data,
                                update = true,
                                self = true
                            ) {
                                when (userForm.isValid()) {
                                    true -> {
                                        UserService.updateMyProfile(
                                            User(
                                                id = user.id,
                                                firstName = userForm.firstName.getValue(),
                                                lastName = userForm.lastName.getValue(),
                                                username = userForm.username.getValue(),
                                                email = userForm.email.getValue(),
                                                roles = user.roles
                                            )
                                        ).then {
                                            window.get("Swal").fire(
                                                Json.encodeToDynamic(
                                                    mapOf(
                                                        "title" to "Success",
                                                        "text" to "Account Updated Successfully.",
                                                        "icon" to "success"
                                                    )
                                                )
                                            )
                                            userFormErrorHandler(null, userForm)
                                            AppEngine.authService.user = it.data
                                            AppEngine.userInfoChangeObserver.setState(it.data)
                                        }.catch { throwable ->
                                            if (throwable is RemoteRequestException) {
                                                if (throwable.code.toInt() == 400) {
                                                    throwable.response?.text()?.then {
                                                        val response: Map<String, List<String>> =
                                                            Json.decodeFromString(it)
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
                    }
                }
            }
        }
    }
}
