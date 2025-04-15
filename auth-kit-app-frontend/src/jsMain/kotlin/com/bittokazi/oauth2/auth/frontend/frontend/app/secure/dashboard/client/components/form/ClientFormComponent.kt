package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.client.components.form

import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormButton
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormControl
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormSelectInput
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormSwitchInput
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormTextInput
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.buttonComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.selectInputComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.switchInputComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.textInputComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Client
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

fun Container.clientFormComponent(
    update: Boolean = false,
    clientForm: ClientForm,
    client: Client?,
    submitCallback: () -> Unit // Client form doesn't seem to handle file uploads
): Container {

    var errorDiv: Div? = null

    val errorMessage: ObservableValue<String> = ObservableValue("")

    val submitForm: ((Event) -> Unit) = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if (!clientForm.isValid()) {
            errorMessage.setState("Invalid Input")
            clientForm.enforceValidation()
        } else {
            clientForm.submitButton.showLoading()
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
                add(
                    textInputComponent(
                        clientForm.clientId,
                        client?.clientId ?: "",
                        disabledInput = !update
                    )
                )
            }
            div(className = "col-md-6") {
                add(textInputComponent(clientForm.resourceIds, client?.resourceIds ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(clientForm.scope, client?.scope?.joinToString(",") ?: ""))
            }
            div(className = "col-md-6") {
                add(
                    selectInputComponent(
                        clientForm.clientAuthenticationMethod,
                        listOf(
                            "client_secret_basic" to "client_secret_basic",
                            "client_secret_post" to "client_secret_post"
                        ),
                        client?.clientAuthenticationMethod ?: ""
                    )
                )
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        clientForm.authorizedGrantTypes,
                        client?.authorizedGrantTypes?.joinToString(",") ?: ""
                    )
                )
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        clientForm.webServerRedirectUri,
                        client?.webServerRedirectUri?.joinToString(",") ?: ""
                    )
                )
            }
            div(className = "col-md-6") {
                add(textInputComponent(clientForm.accessTokenValidity, client?.accessTokenValidity?.toString() ?: ""))
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        clientForm.refreshTokenValidity,
                        client?.refreshTokenValidity?.toString() ?: ""
                    )
                )
            }
            div(className = "col-md-6") {
                add(textInputComponent(clientForm.postLogoutUrl, client?.postLogoutUrl ?: ""))
            }
            div(className = "col-md-6") {
                add(
                    selectInputComponent(
                        clientForm.tokenType,
                        listOf("jwt" to "jwt", "opaque" to "opaque"), // Example options
                        client?.tokenType ?: "bearer"
                    )
                )
            }
            div(className = "col-md-2") {
                add(switchInputComponent(clientForm.requireConsent, client?.requireConsent == true))
            }
            div(className = "col-md-10")
            div(className = "col-md-2") {
                add(
                    switchInputComponent(
                        clientForm.generateSecret,
                        !update,
                        disabledInput = !update
                    )
                )
            }
            div(className = "col-md-10")
            div(className = "col-md-3") {
                add(
                    buttonComponent(
                        clientForm.submitButton,
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
            clientForm.initForm()
        }, 100)
    }
}

class ClientForm(update: Boolean) : FormControl<Unit, Unit> {

    val clientId = FormTextInput(
        label = "Client ID",
        placeholder = when (update) {
            true -> "Please enter unique client ID"
            false -> "Will be auto generated"
        },
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput true
    }

    val resourceIds = FormTextInput(
        label="Resource ID",
        placeholder = "Enter resource id",
        defaultInvalidFeedback = "a to z and _ is allowed",

    ) {
        if(it == null) {
            return@FormTextInput false
        }

        val regex = "^[a-z_]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val scope = FormTextInput(
        label = "Scopes",
        placeholder = "Enter scopes comma(,) separated",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput it != null
    }

    val clientAuthenticationMethod = FormSelectInput(
        label = "Client Authentication Method",
        defaultInvalidFeedback = "Select authentication method"
    ) {
        return@FormSelectInput it != -1
    }

    val authorizedGrantTypes = FormTextInput(
        label = "Authorized Grant Types",
        placeholder = "Enter grant types comma(,) separated",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput it != null
    }

    val webServerRedirectUri = FormTextInput(
        label = "Redirect URL",
        placeholder = "...",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput true
    }

    val accessTokenValidity = FormTextInput(
        label = "Access Token Validity",
        placeholder = "In seconds",
        defaultInvalidFeedback = "Only number is allowed"
    ) {
        if(it == null) {
            return@FormTextInput false
        }

        val regex = "^[0-9]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val refreshTokenValidity = FormTextInput(
        label = "Refresh Token Validity",
        placeholder = "In seconds",
        defaultInvalidFeedback = "Only number is allowed"
    ) {
        if(it == null) {
            return@FormTextInput false
        }

        val regex = "^[0-9]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val requireConsent = FormSwitchInput(
        label = "Require User Consent",
        defaultInvalidFeedback = "Invalid Settings"
    ) {
        return@FormSwitchInput it != null
    }

    val postLogoutUrl = FormTextInput(
        label = "Post Logout URL",
        placeholder = "...",
        defaultInvalidFeedback = "Missing input"
    ) {
        return@FormTextInput true
    }

    val tokenType = FormSelectInput(
        label = "Token Type",
        defaultInvalidFeedback = "Token type missing"
    ) {
        return@FormSelectInput it != -1
    }

    val generateSecret = FormSwitchInput(
        label = "Generate Secret",
        defaultInvalidFeedback = "Invalid Settings"
    ) {
        return@FormSwitchInput it != null
    }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {}

    override fun getInput() {}

    override fun isValid(): Boolean {
        return clientId.isValid()
                && resourceIds.isValid()
                && scope.isValid()
                && clientAuthenticationMethod.isValid()
                && authorizedGrantTypes.isValid()
                && webServerRedirectUri.isValid()
                && accessTokenValidity.isValid()
                && refreshTokenValidity.isValid()
                && requireConsent.isValid()
                && postLogoutUrl.isValid()
                && tokenType.isValid()
                && generateSecret.isValid()
    }

    override fun setCustomError(message: String) {}

    override fun enforceValidation() {
        clientId.enforceValidation()
        resourceIds.enforceValidation()
        scope.enforceValidation()
        clientAuthenticationMethod.enforceValidation()
        authorizedGrantTypes.enforceValidation()
        webServerRedirectUri.enforceValidation()
        accessTokenValidity.enforceValidation()
        refreshTokenValidity.enforceValidation()
        requireConsent.enforceValidation()
        postLogoutUrl.enforceValidation()
        tokenType.enforceValidation()
        generateSecret.enforceValidation()
    }

    override fun getValue() {}

    fun initForm() {
        if (clientId.isValid()) clientId.enforceValidation()
        if (resourceIds.isValid()) resourceIds.enforceValidation()
        if (scope.isValid()) scope.enforceValidation()
        if (clientAuthenticationMethod.isValid()) clientAuthenticationMethod.enforceValidation()
        if (authorizedGrantTypes.isValid()) authorizedGrantTypes.enforceValidation()
        if (webServerRedirectUri.isValid()) webServerRedirectUri.enforceValidation()
        if (accessTokenValidity.isValid()) accessTokenValidity.enforceValidation()
        if (refreshTokenValidity.isValid()) refreshTokenValidity.enforceValidation()
        if (requireConsent.isValid()) requireConsent.enforceValidation()
        if (postLogoutUrl.isValid()) postLogoutUrl.enforceValidation()
        if (tokenType.isValid()) tokenType.enforceValidation()
        if (generateSecret.isValid()) generateSecret.enforceValidation()
    }
}

fun clientFormErrorHandler(
    errors: Map<String, List<String>>?,
    clientForm: ClientForm
) {
    clientForm.submitButton.resetInput()
}
