package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form

import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormButton
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormControl
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormSelectInput
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormTextInput
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.buttonComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.selectInputComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.textInputComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.core.Container
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.html.Div
import io.kvision.html.InputType
import io.kvision.html.div
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import org.w3c.dom.events.Event

fun Container.userFormComponent(
    update: Boolean = false,
    userForm: UserForm,
    user: User?,
    rolesList: List<Role> = emptyList(),
    self: Boolean = false,
    submitCallback: () -> Unit
): Container {

    var errorDiv: Div? = null
    val errorMessage: ObservableValue<String> = ObservableValue("")

    val submitForm: ((Event) -> Unit) = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if (!userForm.isValid()) {
            errorMessage.setState("Invalid Input")
            userForm.enforceValidation()
        } else {
            userForm.submitButton.showLoading()
            submitCallback()
        }
    }

    errorMessage.subscribe {
        errorDiv?.getElementJQuery()?.slideToggle()
        when (it) {
            "" -> errorDiv?.getElementJQuery()?.fadeOut(300, "linear")
            else -> {
                errorDiv?.getElementJQuery()?.fadeIn(300, "linear")
            }
        }
    }

    return form {
        div(className = "row mb-3") {
            div(className = "col-md-6") {
                add(textInputComponent(userForm.firstName, user?.firstName ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(userForm.lastName, user?.lastName ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(userForm.email, user?.email ?: ""))
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        userForm.username,
                        user?.username ?: "",
                        disabledInput = update // Disable username on update
                    )
                )
            }
            if (!self) {
                div(className = "col-md-6") {
                    add(
                        selectInputComponent(
                            userForm.roles,
                            rolesList.map { it.id.toString() to it.title!! },
                            user?.roles?.firstOrNull()?.id
                        )
                    )
                }
            }
            if (!update) {
                div(className = "col-md-6") {
                    add(textInputComponent(userForm.newPassword, "", inputType = InputType.PASSWORD))
                }
            } else {
                if (!self) {
                    div(className = "col-md-6")
                }
            }
            div(className = "col-md-3") {
                add(buttonComponent(userForm.submitButton, if (update) "Update" else "Add"))
            }
            div(className = "col-md-9")
            div(className = "col-md-3") {
                window.setTimeout({
                    if (errorDiv == null) {
                        errorDiv = Div(className = "alert alert-danger align-items-center") {
                            marginTop = 10 to UNIT.px
                            setAttribute("role", "alert")
                            div {
                                errorMessage.subscribe {
                                    content = it
                                }
                            }
                        }
                        add(errorDiv!!)
                        errorDiv?.getElementJQuery()?.hide(0)
                    }
                }, 50)
            }
        }
        onEvent {
            submit = submitForm
        }
        window.setTimeout({
            userForm.initForm()
        }, 100)
    }
}

fun Container.userPasswordFormComponent(
    self: Boolean = false,
    userPasswordUpdateForm: UserPasswordUpdateForm,
    submitCallback: () -> Unit
): Container {

    var errorDiv: Div? = null
    val errorMessage: ObservableValue<String> = ObservableValue("")

    val submitForm: ((Event) -> Unit) = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if (!userPasswordUpdateForm.isValid()) {
            errorMessage.setState("Invalid Input")
            userPasswordUpdateForm.enforceValidation()
        } else {
            userPasswordUpdateForm.submitButton.showLoading()
            submitCallback()
        }
    }

    errorMessage.subscribe {
        errorDiv?.getElementJQuery()?.slideToggle()
        when (it) {
            "" -> errorDiv?.getElementJQuery()?.fadeOut(300, "linear")
            else -> {
                errorDiv?.getElementJQuery()?.fadeIn(300, "linear")
            }
        }
    }

    return form {
        div(className = "row") {
            if (!self) {
                div(className = "col-md-6") {
                    add(textInputComponent(userPasswordUpdateForm.newPassword, "", inputType = InputType.PASSWORD))
                }
                div(className = "col-md-6")
            } else{
                div(className = "col-md-6") {
                    add(textInputComponent(userPasswordUpdateForm.currentPassword, "", inputType = InputType.PASSWORD))
                }
                div(className = "col-md-6")
                div(className = "col-md-6") {
                    add(textInputComponent(userPasswordUpdateForm.newPassword, "", inputType = InputType.PASSWORD))
                }
                div(className = "col-md-6")
                div(className = "col-md-6") {
                    add(
                        textInputComponent(
                            userPasswordUpdateForm.newConfirmPassword, "", inputType = InputType.PASSWORD
                        )
                    )
                }
                div(className = "col-md-6")
            }
            div(className = "col-md-3") {
                add(buttonComponent(userPasswordUpdateForm.submitButton, "Update Password"))
            }
            div(className = "col-md-9")
            div(className = "col-md-3") {
                window.setTimeout({
                    if (errorDiv == null) {
                        errorDiv = Div(className = "alert alert-danger align-items-center") {
                            marginTop = 10 to UNIT.px
                            setAttribute("role", "alert")
                            div {
                                errorMessage.subscribe {
                                    content = it
                                }
                            }
                        }
                        add(errorDiv!!)
                        errorDiv?.getElementJQuery()?.hide(0)
                    }
                }, 50)
            }
        }
        onEvent {
            submit = submitForm
        }
    }
}

class UserForm(val update: Boolean, val self: Boolean = false) : FormControl<Unit, Unit> {

    val firstName = FormTextInput(
        label = "First Name",
        placeholder = "Enter first name",
        defaultInvalidFeedback = "First name is required"
    ) {
        return@FormTextInput !it.isNullOrBlank()
    }

    val lastName = FormTextInput(
        label = "Last Name",
        placeholder = "Enter last name",
        defaultInvalidFeedback = "Last name is required"
    ) {
        return@FormTextInput !it.isNullOrBlank()
    }

    val email = FormTextInput(
        label = "Email",
        placeholder = "Enter email address",
        defaultInvalidFeedback = "Invalid email format"
    ) {
        return@FormTextInput it?.matches(Regex("[^@]+@[^\\.]+\\..+")) ?: false
    }

    val username = FormTextInput(
        label = "Username",
        placeholder = "Enter username",
        defaultInvalidFeedback = "Username is required"
    ) {
        return@FormTextInput return@FormTextInput it?.matches(Regex("^[a-z0-9](?:[a-z0-9._]*[a-z0-9])?\$")) ?: false
    }

    val roles = FormSelectInput(
        label = "Roles",
        defaultInvalidFeedback = "At least one role must be selected"
    ) {
        return@FormSelectInput it != -1
    }

    val newPassword = FormTextInput(
        label = "New Password",
        placeholder = "Enter new password",
        defaultInvalidFeedback = "New password must be at least 8 characters long"
    ) {
        return@FormTextInput (it?.length ?: 0) >= 8
    }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {}
    override fun getInput() {}

    override fun isValid(): Boolean {
        var isValid = firstName.isValid()
                && lastName.isValid()
                && email.isValid()
                && username.isValid()

        if (!self) {
            isValid = isValid && roles.isValid()
        }

        if (!update) {
            isValid = isValid && newPassword.isValid()
        }
        return isValid
    }

    override fun setCustomError(message: String) {
        // Implement custom error setting for specific fields if needed
    }

    override fun enforceValidation() {
        firstName.enforceValidation()
        lastName.enforceValidation()
        email.enforceValidation()
        username.enforceValidation()

        if (!self) {
            roles.enforceValidation()
        }

        if (!update) {
            newPassword.enforceValidation()
        }
    }

    override fun getValue() {}

    fun initForm() {
        if (firstName.isValid()) firstName.enforceValidation()
        if (lastName.isValid()) lastName.enforceValidation()
        if (email.isValid()) email.enforceValidation()
        if (username.isValid()) username.enforceValidation()
        if (!self && roles.isValid()) roles.enforceValidation()
        if (!update && newPassword.isValid()) {
            newPassword.enforceValidation()
        }
    }
}

class UserPasswordUpdateForm(val self: Boolean) : FormControl<Unit, Unit> {

    val currentPassword = FormTextInput(
        label = "Current Password",
        placeholder = "Enter your current password",
        defaultInvalidFeedback = "Missing current password"
    ) {
        return@FormTextInput !it.isNullOrBlank()
    }

    val newPassword = FormTextInput(
        label = "New Password",
        placeholder = "Enter new password",
        defaultInvalidFeedback = "New password must be at least 8 characters long"
    ) {
        if (self) {
            val regex = Regex("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$")
            return@FormTextInput it?.matches(regex) ?: false
        }
        return@FormTextInput (it?.length ?: 0) >= 8
    }

    val newConfirmPassword = FormTextInput(
        label = "Repeat New Password",
        placeholder = "Enter your new password again",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput !it.isNullOrBlank()
    }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {
        TODO("Not yet implemented")
    }

    override fun getInput() {
        TODO("Not yet implemented")
    }

    override fun isValid(): Boolean {
        return when (self) {
            true -> currentPassword.isValid()
                    && newPassword.isValid()
                    && newConfirmPassword.isValid()

            false -> newPassword.isValid()
        }
    }

    override fun setCustomError(message: String) {
        TODO("Not yet implemented")
    }

    override fun enforceValidation() {
        when (self) {
            true -> {
                currentPassword.enforceValidation()
                newPassword.enforceValidation()
                newConfirmPassword.enforceValidation()
            }

            false -> {
                newPassword.enforceValidation()
            }
        }
    }

    override fun getValue() {
        TODO("Not yet implemented")
    }
}

fun userFormErrorHandler(
    errors: Map<String, List<String>>?,
    userForm: UserForm
) {
    if (errors != null) {
        if (errors["username"]?.contains("exist") == true) {
            userForm.username.setCustomError("Username already exist")
        }
        if (errors["username"]?.contains("notAllowed") == true) {
            userForm.username.setCustomError("Username not allowed")
        }
        if (errors["email"]?.contains("exist") == true) {
            userForm.email.setCustomError("Email already exist")
        }
        if (errors["role"]?.contains("empty") == true) {
            userForm.roles.setCustomError("Role must be selected")
        }
    }

    userForm.submitButton.resetInput()
}

fun userPasswordFormErrorHandler(
    errors: Map<String, List<String>>?,
    userPasswordUpdateForm: UserPasswordUpdateForm,
) {
    if (errors != null) {
        if (errors["currentPassword"]?.contains("currentWrong") == true) {
            userPasswordUpdateForm.currentPassword.setCustomError("Current password is wrong")
        }
        if (errors["newPassword"]?.contains("sameToPrevious") == true) {
            userPasswordUpdateForm.newPassword.setCustomError("New password is same as the current password")
        }
        if (errors["newConfirmPassword"]?.contains("newDoNotMatch") == true) {
            userPasswordUpdateForm.newConfirmPassword.setCustomError("Passwords do not match")
        }
    }

    userPasswordUpdateForm.submitButton.resetInput()
}
