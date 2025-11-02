package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.form

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.components.form.FormButton
import com.bittokazi.kvision.spa.framework.base.components.form.FormControl
import com.bittokazi.kvision.spa.framework.base.components.form.FormSwitchInput
import com.bittokazi.kvision.spa.framework.base.components.form.FormTextInput
import com.bittokazi.kvision.spa.framework.base.components.form.FormUploadInput
import com.bittokazi.kvision.spa.framework.base.components.form.buttonComponent
import com.bittokazi.kvision.spa.framework.base.components.form.switchInputComponent
import com.bittokazi.kvision.spa.framework.base.components.form.textInputComponent
import com.bittokazi.kvision.spa.framework.base.components.form.uploadInputComponent
import com.bittokazi.kvision.spa.framework.base.models.SpaResult
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.restService
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Tenant
import io.kvision.core.Container
import io.kvision.core.UNIT
import io.kvision.core.getElementJQuery
import io.kvision.core.onEvent
import io.kvision.form.form
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.serialization.json.JsonObject
import org.w3c.dom.events.Event

fun Container.tenantFormComponent(
    update: Boolean = false,
    tenantForm: TenantForm,
    tenant: Tenant?,
    submitCallback: (JsonObject?) -> Unit
): Container {

    var errorDiv: Div? = null

    val errorMessage: ObservableValue<String> = ObservableValue("")

    val uploadFile: ((fn: (JsonObject?) -> Unit) -> Unit) = { fn ->
        when (tenantForm.customTemplateLocation.input.value) {
            null -> fn(null)
            else -> SpaAppEngine.fileService.upload(
                fileName = tenantForm.companyKey.getValue(),
                uploadInput = tenantForm.customTemplateLocation.input,
                url = "${restService.BASE_URL}/api/tenants/templates"
            ) {
                console.log("[upload response] -> ${it}")

                when (it) {
                    is SpaResult.Failure -> {
                        tenantForm.submitButton.resetInput()
                        errorMessage.setState("Unable to upload file [Server Error]")
                    }
                    is SpaResult.Success -> fn(it.outcome)
                }
            }
        }
    }

    val submitForm: ((Event) -> Unit) = { ev ->
        ev.preventDefault()
        if (errorMessage.value != "") {
            errorMessage.setState("")
        }

        if(!tenantForm.isValid()) {
            errorMessage.setState("Invalid Input")
            tenantForm.enforceValidation()
        } else {
            tenantForm.submitButton.showLoading()

            uploadFile { uploadResponse ->
                submitCallback(uploadResponse)
            }
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
                add(textInputComponent(tenantForm.name, tenant?.name ?: ""))
            }
            div(className = "col-md-6") {
                add(
                    textInputComponent(
                        tenantForm.companyKey,
                        tenant?.companyKey ?: "",
                        disabledInput = update
                    )
                )
            }
            div(className = "col-md-6") {
                add(textInputComponent(tenantForm.domain, tenant?.domain ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(tenantForm.signInBtnColor, tenant?.signInBtnColor ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(tenantForm.resetPasswordLink, tenant?.resetPasswordLink ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(tenantForm.createAccountLink, tenant?.createAccountLink ?: ""))
            }
            div(className = "col-md-6") {
                add(textInputComponent(tenantForm.defaultRedirectUrl, tenant?.defaultRedirectUrl ?: ""))
            }
            div(className = "col-md-6") {
                add(uploadInputComponent(tenantForm.customTemplateLocation, disabledInput = !update))
            }
            div(className = "col-md-2") {
                add(switchInputComponent(tenantForm.enableConfigPanel, tenant?.enableConfigPanel == true))
            }
            div(className = "col-md-10")
            div(className = "col-md-2") {
                add(switchInputComponent(tenantForm.enableCustomTemplate, tenant?.enableCustomTemplate == true))
            }
            div(className = "col-md-10")
            div(className = "col-md-2") {
                add(switchInputComponent(tenantForm.enabled, tenant?.enabled == true))
            }
            div(className = "col-md-10")
            div(className = "col-md-3") {
                add(
                    buttonComponent(
                        tenantForm.submitButton,
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
        window.setTimeout({
            tenantForm.initForm()
        }, 100)
    }
}

class TenantForm : FormControl<Unit, Unit> {

    val name = FormTextInput(
        label = "Name",
        placeholder = "Enter tenant name",
        defaultInvalidFeedback = "Name missing"
    ) {
        return@FormTextInput !(it == null || it.isEmpty())
    }

    val companyKey = FormTextInput(
        label = "Key",
        placeholder = "Enter unique tenant key here",
        defaultInvalidFeedback = "Valid characters are [a-z, 0-9 and _]"
    ) {
        if(it == null) {
            return@FormTextInput false
        }
        val regex = "^[a-z0-9](?:_[a-z0-9]+|[a-z0-9])*\$".toRegex()
        return@FormTextInput regex.matches(it)
    }

    val domain = FormTextInput(
        label = "Domain",
        placeholder = "Enter domain name",
        defaultInvalidFeedback = "Invalid domain name"
    ) {
        return@FormTextInput !(it == null || it.length < 5)
    }

    val signInBtnColor = FormTextInput(
        label = "Sign In Button Color",
        placeholder = "Please enter valid button color",
        defaultInvalidFeedback = "Invalid color"
    ) {
        return@FormTextInput true
    }

    val resetPasswordLink = FormTextInput(
        label = "Reset Password Link",
        placeholder = "Leave blank if you don't want to show",
        defaultInvalidFeedback = "Invalid link"
    ) {
        return@FormTextInput true
    }

    val createAccountLink = FormTextInput(
        label = "Create Account Link",
        placeholder = "Leave blank if you don't want to show",
        defaultInvalidFeedback = "Invalid link"
    ) {
        return@FormTextInput true
    }

    val enabled = FormSwitchInput(
        label = "Tenant Enabled",
        defaultInvalidFeedback = "Invalid Settings"
    ) {
        return@FormSwitchInput it != null
    }

    val defaultRedirectUrl = FormTextInput(
        label = "Default Redirect URL",
        placeholder = "Default redirect link after login",
        defaultInvalidFeedback = "Invalid URL"
    ) {
        return@FormTextInput true
    }

    val enableConfigPanel = FormSwitchInput(
        label = "Enabled Config Panel",
        defaultInvalidFeedback = "Invalid Settings"
    ) {
        return@FormSwitchInput it != null
    }

    val enableCustomTemplate = FormSwitchInput(
        label = "Enabled Custom Template",
        defaultInvalidFeedback = "Invalid Settings"
    ) {
        return@FormSwitchInput it != null
    }

    val customTemplateLocation = FormUploadInput(
        label = "Custom Template File",
        accept = listOf(".zip")
    ) {
        return@FormUploadInput true
    }

    val submitButton = FormButton()

    override fun setInput(input: Unit) {}

    override fun getInput() {}

    override fun isValid(): Boolean {
        return name.isValid()
                && companyKey.isValid()
                && domain.isValid()
                && signInBtnColor.isValid()
                && resetPasswordLink.isValid()
                && createAccountLink.isValid()
                && enabled.isValid()
                && defaultRedirectUrl.isValid()
                && enableConfigPanel.isValid()
                && enableCustomTemplate.isValid()
                && customTemplateLocation.isValid()
    }

    override fun setCustomError(message: String) {}

    override fun enforceValidation() {
        name.enforceValidation()
        companyKey.enforceValidation()
        domain.enforceValidation()
        signInBtnColor.enforceValidation()
        resetPasswordLink.enforceValidation()
        createAccountLink.enforceValidation()
        enabled.enforceValidation()
        defaultRedirectUrl.enforceValidation()
        enableConfigPanel.enforceValidation()
        enableCustomTemplate.enforceValidation()
        customTemplateLocation.enforceValidation()
    }

    override fun getValue() {}

    fun initForm() {
        if (name.isValid()) name.enforceValidation()
        if (companyKey.isValid()) companyKey.enforceValidation()
        if (domain.isValid()) domain.enforceValidation()
        if (signInBtnColor.isValid()) signInBtnColor.enforceValidation()
        if (resetPasswordLink.isValid()) resetPasswordLink.enforceValidation()
        if (createAccountLink.isValid()) createAccountLink.enforceValidation()
        if (enabled.isValid()) enabled.enforceValidation()
        if (defaultRedirectUrl.isValid()) defaultRedirectUrl.enforceValidation()
        if (enableConfigPanel.isValid()) enableConfigPanel.enforceValidation()
        if (enableCustomTemplate.isValid()) enableCustomTemplate.enforceValidation()
        if (customTemplateLocation.isValid()) customTemplateLocation.enforceValidation()
    }
}

fun tenantFormErrorHandler(
    errors: Map<String, List<String>>?,
    tenantForm: TenantForm
) {
    if (errors != null) {
        if (errors["name"]?.contains("exist") == true) {
            tenantForm.name.setCustomError("Company name already exist")
        }
        if (errors["key"]?.contains("exist") == true) {
            tenantForm.companyKey.setCustomError("Company key already exist")
        }
        if (errors["domain"]?.contains("exist") == true) {
            tenantForm.domain.setCustomError("Domain already exist")
        }
    }

    tenantForm.submitButton.resetInput()
}
