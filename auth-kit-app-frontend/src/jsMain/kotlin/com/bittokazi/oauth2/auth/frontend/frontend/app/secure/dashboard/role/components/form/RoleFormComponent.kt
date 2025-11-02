package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.components.form

import com.bittokazi.kvision.spa.framework.base.components.form.FormButton
import com.bittokazi.kvision.spa.framework.base.components.form.FormControl
import com.bittokazi.kvision.spa.framework.base.components.form.FormTextInput
import com.bittokazi.kvision.spa.framework.base.components.form.buttonComponent
import com.bittokazi.kvision.spa.framework.base.components.form.textInputComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Role
import io.kvision.core.Container
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import org.w3c.dom.events.Event

fun Container.roleFormComponent(
    update: Boolean = false,
    roleForm: RoleForm,
    role: Role?,
    submitCallback: () -> Unit
): Container {

    var errorDiv: Div? = null

    val errorMessage: ObservableValue<String> = ObservableValue("")

    val submitForm: ((Event) -> Unit) = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if(!roleForm.isValid()) {
            errorMessage.setState("Invalid Form")
            roleForm.enforceValidation()
        } else {
            roleForm.submitButton.showLoading()
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
            div(className = "col-md-6") {
                add(textInputComponent(roleForm.name, role?.name ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(roleForm.title, role?.title ?: ""))
            }
            div(className = "col-md-3") {
                add(
                    buttonComponent(
                        roleForm.submitButton,
                        when (update) {
                            true -> "Update"
                            false -> "Add"
                        }
                    )
                )
            }
            div(className = "col-md-9")
            div(className = "col-md-3") {
                window.setTimeout({
                    if(errorDiv == null) {
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

class RoleForm : FormControl<Unit, Unit> {

    val title = FormTextInput(
        label = "Title",
        placeholder = "Enter role title",
        defaultInvalidFeedback = "Title missing"
    ) {
        return@FormTextInput !(it == null || it.isEmpty())
    }

    val name = FormTextInput(
        label = "Name",
        placeholder = "ROLE_*",
        defaultInvalidFeedback = "Please provide a role name consist with all upper case letters and underscore"
    ) {
        if(it == null) {
            return@FormTextInput false
        }
        val regex = "^ROLE_[A-Z_]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val submitButton = FormButton()
    override fun setInput(input: Unit) {
        TODO("Not yet implemented")
    }

    override fun getInput() {
        TODO("Not yet implemented")
    }

    override fun isValid(): Boolean {
        return title.isValid()
                && name.isValid()
    }

    override fun setCustomError(message: String) {
        TODO("Not yet implemented")
    }

    override fun enforceValidation() {
        name.enforceValidation()
        title.enforceValidation()
    }

    override fun getValue() {
        TODO("Not yet implemented")
    }
}

fun roleFormErrorHandler(
    errors: Map<String, List<String>>?,
    roleForm: RoleForm
) {
    if (errors != null) {
        if (errors["name"]?.contains("exist") == true) {
            roleForm.name.setCustomError("Role name already exist")
        }
    }

    roleForm.submitButton.resetInput()
}
