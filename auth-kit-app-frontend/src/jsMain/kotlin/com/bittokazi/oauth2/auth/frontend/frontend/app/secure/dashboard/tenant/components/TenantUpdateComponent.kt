package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.TenantService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.form.TenantForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.form.tenantFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.tenant.components.form.tenantFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_TENANT_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.Tenant
import io.kvision.core.Container
import io.kvision.html.div
import io.kvision.rest.RemoteRequestException
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
fun Container.tenantUpdateComponent(
    id: String
): Container {
    val tenantForm = TenantForm()

    return div {
        TenantService.get(id).then { tenant ->
            tenantFormComponent(
                update = true,
                tenantForm = tenantForm,
                tenant = tenant.data
            ) { fileResponse ->
                if (tenantForm.isValid()) {
                    tenantForm.submitButton.showLoading()
                    TenantService.update(
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
                            customTemplateLocation = fileResponse?.absoluteFilePath ?: ""
                        )
                    ).then {
                        window.get("Swal").fire(
                            Json.encodeToDynamic(
                                mapOf(
                                    "title" to "Success",
                                    "text" to "Updated Tenant Successfully.",
                                    "icon" to "success"
                                )
                            )
                        )
                        AppEngine.routing.navigate(APP_DASHBOARD_TENANT_ROUTE)
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
