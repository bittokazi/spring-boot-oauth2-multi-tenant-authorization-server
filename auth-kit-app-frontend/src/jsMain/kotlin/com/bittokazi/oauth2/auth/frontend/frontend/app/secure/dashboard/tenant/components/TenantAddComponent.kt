package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine
import com.bittokazi.kvision.spa.framework.base.utils.sweetAlert
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.form.TenantForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.form.tenantFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.form.tenantFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.tenantService
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Tenant
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.rest.RemoteRequestException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

@OptIn(ExperimentalSerializationApi::class)
class TenantAddComponent: SimplePanel() {

    val tenantForm = TenantForm()

    init {
        div {
            tenantFormComponent(
                update = false,
                tenantForm = tenantForm,
                tenant = null
            ) { fileResponse ->
                if (tenantForm.isValid()) {
                    tenantForm.submitButton.showLoading()
                    tenantService.create(
                        Tenant(
                            id = id,
                            name = tenantForm.name.getValue(),
                            companyKey = tenantForm.companyKey.getValue(),
                            domain = tenantForm.domain.getValue(),
                            enabled = tenantForm.enabled.getValue(),
                            signInBtnColor = tenantForm.signInBtnColor.getValue(),
                            createAccountLink = tenantForm.createAccountLink.getValue(),
                            resetPasswordLink = tenantForm.resetPasswordLink.getValue(),
                            defaultRedirectUrl = tenantForm.defaultRedirectUrl.getValue(),
                            enableConfigPanel = tenantForm.enableConfigPanel.getValue(),
                            enableCustomTemplate = tenantForm.enableCustomTemplate.getValue(),
                            customTemplateLocation = fileResponse?.get("absoluteFilePath")?.toString() ?: ""
                        )
                    ).then {
                        sweetAlert.fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Success",
                                    "text" to "Added Tenant Successfully.",
                                    "icon" to "success"
                                )
                            )
                        )
                        SpaAppEngine.routing.navigate(APP_DASHBOARD_TENANT_ROUTE)
                    }.catch { throwable ->
                        if (throwable is RemoteRequestException) {
                            if (throwable.code.toInt() == 400) {
                                throwable.response?.text()?.then {
                                    val response: Map<String, List<String>> = Json.decodeFromString(it)
                                    return@then tenantFormErrorHandler(response, tenantForm)
                                }
                            }
                        }
                        tenantFormErrorHandler(null, tenantForm)
                    }
                }
            }
        }
    }
}
