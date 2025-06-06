package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.account.components

import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.UserService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.UserPasswordUpdateForm
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userPasswordFormComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.userPasswordFormErrorHandler
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SECURITY_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine.APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.ObservableManager
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormButton
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.FormTextInput
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.buttonComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.form.textInputComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.modal.BootstrapModalService
import com.bittokazi.oauth2.auth.frontend.frontend.base.components.modal.bootstrapModalComponent
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.TwoFASecretPayload
import com.bittokazi.oauth2.auth.frontend.frontend.base.services.QrCodeService
import com.bittokazi.oauth2.auth.frontend.frontend.base.utils.Utils
import io.kvision.core.Col
import io.kvision.core.Color
import io.kvision.core.Container
import io.kvision.core.Cursor
import io.kvision.core.onClick
import io.kvision.html.Div
import io.kvision.html.Tbody
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.html.h4
import io.kvision.html.link
import io.kvision.html.nav
import io.kvision.html.span
import io.kvision.html.table
import io.kvision.html.tbody
import io.kvision.html.td
import io.kvision.html.th
import io.kvision.html.thead
import io.kvision.html.tr
import io.kvision.rest.RemoteRequestException
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
fun Container.securitySettingsComponent(): Container {
    val userPasswordUpdateForm = UserPasswordUpdateForm(self = true)
    var user: User
    lateinit var twoFASecretPayload: TwoFASecretPayload
    val pageRefreshListner = ObservableValue<Boolean>(false)
    val twoFaModalChangeListner = ObservableValue<Boolean>(false)
    val deleteObserver = ObservableValue<Boolean?>(null)
    val twoFaCodeInput = FormTextInput("", "Enter the code shown in 2FA App",
        defaultInvalidFeedback = "Only numbers allowed") {
        if(it == null) {
            return@FormTextInput false
        }

        val regex = "^[0-9]+$".toRegex()
        return@FormTextInput regex.matches(it)
    }
    val twoFaCodeInputButton = FormButton()

    fun sendTwoFaEnableRequest(code: String = "")  {
        if (!twoFaCodeInput.isValid()) {
           twoFaCodeInput.enforceValidation()
            return
        }
        twoFASecretPayload.code = code.toInt()
        UserService.enableTwoFa(
            twoFASecretPayload
        ).then {
            when (it.data.twoFaEnabled) {
                true -> {
                    window.get("Swal").fire(
                        Json.encodeToDynamic(
                            mapOf(
                                "title" to "Enabled 2FA",
                                "type" to "warning",
                                "confirmButtonColor" to "#3085d6",
                                "cancelButtonColor" to "#d33",
                                "confirmButtonText" to "Dismiss",
                                "allowOutsideClick" to null,
                                "html" to "<p style=\"text-align: justify\">" +
                                        "<span style=\"font-weight: bold;\">Please keep the backup codes in safe place" +
                                        "</span><br /><br/>" +
                                        twoFASecretPayload.scratchCodes?.mapIndexed { i, s -> "${i+1}. $s" }
                                            ?.joinToString("<br />") +
                                        "</p>"
                            )
                        )
                    )
                    twoFaModalChangeListner.setState(false)
                    window.setTimeout({
                        pageRefreshListner.setState(true)
                    },100)
                }
                else -> {
                    twoFaCodeInput.setCustomError("Invalid code")
                }
            }
        }.catch {}
    }

    fun disableTwoFa(onFinish: () -> Unit) {
        UserService.disableTwoFa().then {
            when (it.data.twoFaEnabled) {
                false -> {
                    window.get("Swal").fire(
                        Json.encodeToDynamic(
                            mapOf(
                                "title" to "Success",
                                "text" to "Disabled 2FA Successfully.",
                                "icon" to "success"
                            )
                        )
                    )
                    pageRefreshListner.setState(true)
                }

                else -> {
                    pageRefreshListner.setState(true)
                }
            }
            onFinish()
        }.catch {
            onFinish()
        }
    }

    fun reGenerateScratchCodes(onFinish: () -> Unit) {
        UserService.reGenerateScratchCodes().then {
            window.get("Swal").fire(
                Json.encodeToDynamic(
                    mapOf(
                        "title" to "Success",
                        "type" to "warning",
                        "confirmButtonColor" to "#3085d6",
                        "cancelButtonColor" to "#d33",
                        "confirmButtonText" to "Dismiss",
                        "allowOutsideClick" to null,
                        "html" to "<p style=\"text-align: justify\">" +
                                "<span style=\"font-weight: bold;\">Please keep the backup codes in safe place" +
                                "</span><br /><br />" +
                                it.data.mapIndexed { i, s -> "${i+1}. $s" }.joinToString("<br />") +
                                "</p>"
                    )
                )
            )
            onFinish()
        }.catch {
            onFinish()
        }
    }

    fun delete(id: Long) {
        UserService.deleteTrustedDevice(id).then {
            window.get("Swal").fire(
                Json.encodeToDynamic(
                    mapOf(
                        "title" to "Success",
                        "text" to "Removed device with ID [$id]",
                        "icon" to "success"
                    )
                )
            )
            deleteObserver.setState(true)
        }.catch {
            window.get("Swal").fire(
                Json.encodeToDynamic(
                    mapOf(
                        "title" to "Error",
                        "text" to "Unable to delete",
                        "icon" to "error"
                    )
                )
            )
            deleteObserver.setState(true)
        }
    }

    fun trustedDevicesTable(): Div {
        return div(className = "table-responsive") {
            table(className = "table table-hover my-0") {
                thead {
                    tr {
                        th {
                            content = "#"
                        }
                        th {
                            content = "IP"
                        }
                        th {
                            content = "User Agent"
                        }
                        th {
                            content = "Logged On"
                        }
                        th {
                            content = "Actions"
                        }
                    }
                }

                fun tableBody(): Tbody {
                    return tbody {
                        UserService.getTrustedDevices().then { response ->
                            response.data.forEachIndexed {  index, device ->
                                tr {
                                    td {
                                        content = device.id.toString()
                                    }
                                    td {
                                        content = device.deviceIp
                                    }
                                    td {
                                        content = device.userAgent
                                    }
                                    td {
                                        content = Utils.formatTimeFromNow(device.createdDate)
                                    }
                                    td {
                                        span {
                                            color = Color.name(
                                                Col.RED
                                            )
                                            cursor = Cursor.POINTER
                                            onClick {
                                                delete(device.id!!)
                                                hide()
                                            }
                                            span(className = "feather-sm me-1") {
                                                setAttribute("data-feather", "trash")
                                            }
                                            + "Delete"
                                        }
                                    }
                                }
                            }
                        }.then {
                            window.setTimeout({
                                window["feather"].replace()
                            }, 100)

                            AppEngine.routing.updatePageLinks()
                        }
                    }
                }
                add(tableBody())

                deleteObserver.subscribe {
                    if(it !=null && it) {
                        removeAt(1)
                        add(tableBody())
                        window.setTimeout({
                            window["feather"].replace()
                        }, 100)
                    }
                }
            }
        }
    }

    fun contentBody(): Div {
        return div {
            div(className = "row mt-3") {
                nav(className = "nav nav-pills nav-justified") {
                    link(
                        label = "",
                        url = APP_DASHBOARD_ACCOUNT_SETTINGS_ROUTE,
                        className = "nav-link",
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
                        className = "nav-link active",
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
                        div(className = "mb-3") {
                            h4("Password Settings")
                        }
                        div {
                            AppEngine.authService.whoAmI().then { userResponse ->
                                user = userResponse.data
                                userPasswordFormComponent(
                                    self = true,
                                    userPasswordUpdateForm = userPasswordUpdateForm
                                ) {
                                    when (userPasswordUpdateForm.isValid()) {
                                        true -> {
                                            UserService.updateMyPassword(
                                                User(
                                                    id = user.id,
                                                    currentPassword = userPasswordUpdateForm.currentPassword.getValue(),
                                                    newPassword = userPasswordUpdateForm.newPassword.getValue(),
                                                    newConfirmPassword = userPasswordUpdateForm.newConfirmPassword
                                                        .getValue()
                                                )
                                            ).then {
                                                window.get("Swal").fire(
                                                    Json.encodeToDynamic(
                                                        mapOf(
                                                            "title" to "Success",
                                                            "text" to "Updated Password Successfully.",
                                                            "icon" to "success"
                                                        )
                                                    )
                                                )
                                                pageRefreshListner.setState(true)
                                            }.catch { throwable ->
                                                if (throwable is RemoteRequestException) {
                                                    if (throwable.code.toInt() == 400) {
                                                        throwable.response?.text()?.then {
                                                            val response: Map<String, List<String>> =
                                                                Json.decodeFromString(it)
                                                            return@then userPasswordFormErrorHandler(response,
                                                                userPasswordUpdateForm)
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
                                div(className = "row mt-4") {
                                    div(className = "mb-3") {
                                        h4("2FA Authentication Settings")
                                    }
                                    div(className = "col-md-3") {
                                        when (user.twoFaEnabled) {
                                            true -> {
                                                div(className = "d-grid gap-2") {
                                                    button(text = "", className = "btn btn-danger") {
                                                        span(className = "feather-sm") {
                                                            setAttribute("data-feather", "shield-off")
                                                        }
                                                        +" Disable 2FA"

                                                        onClick {
                                                            disabled = true
                                                            disableTwoFa {
                                                                disabled = false
                                                            }
                                                        }
                                                    }
                                                }
                                                div(className = "d-grid gap-2 mt-3") {
                                                    button(text = "", className = "btn btn-warning") {
                                                        span(className = "feather-sm me-1") {
                                                            setAttribute("data-feather", "refresh-cw")
                                                        }
                                                        +" Regenerate Backup Codes"

                                                        onClick {
                                                            disabled = true
                                                            reGenerateScratchCodes {
                                                                disabled = false
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            else -> {
                                                div(className = "d-grid gap-2") {
                                                    button(text = "", className = "btn btn-success") {
                                                        span(className = "feather-sm") {
                                                            setAttribute("data-feather", "shield")
                                                        }
                                                        +" Enable 2FA"

                                                        onClick {
                                                            disabled = true
                                                            UserService.generateSecret().then { secretPayload ->
                                                                twoFASecretPayload = secretPayload.data
                                                                twoFaModalChangeListner.setState(true)
                                                                window.setTimeout({
                                                                    BootstrapModalService.open {
                                                                        js("new bootstrap.Modal('#twoFa')")
                                                                    }
                                                                    QrCodeService.create(
                                                                        secretPayload.data,
                                                                        "qrcode",
                                                                        user.username
                                                                    ) {
                                                                        js(
                                                                            "new QRCode(\"qrcode\", {\n" +
                                                                                    "    text: \"n/a\",\n" +
                                                                                    "    width: 128,\n" +
                                                                                    "    height: 128,\n" +
                                                                                    "    colorDark : \"#000000\",\n" +
                                                                                    "    colorLight : \"#ffffff\",\n" +
                                                                                    "    correctLevel : " +
                                                                                    "QRCode.CorrectLevel.H\n" +
                                                                                    "})"
                                                                        )
                                                                    }
                                                                }, 300)
                                                                window.setTimeout({
                                                                    disabled = false
                                                                }, 700)
                                                            }.catch { throwable ->
                                                                if (throwable is RemoteRequestException) {
                                                                    if (throwable.code.toInt() == 403) {
                                                                        window.get("Swal").fire(
                                                                            Json.encodeToDynamic(
                                                                                mapOf(
                                                                                    "title" to "Error",
                                                                                    "text" to "Access Denied [403]",
                                                                                    "icon" to "error"
                                                                                )
                                                                            )
                                                                        )
                                                                    }
                                                                }
                                                                disabled = false
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                when (user.twoFaEnabled) {
                                    true -> {
                                        div(className = "row mt-4") {
                                            div(className = "col-md-6") {
                                                div(className = "accordion") {
                                                    div(className = "accordion-item") {
                                                        h2(className = "accordion-header") {
                                                            button("Trusted Devices", className = "accordion-button") {
                                                                setAttribute("data-bs-toggle", "collapse")
                                                                setAttribute("data-bs-target", "#panelsStayOpen-collapseOne")
                                                                setAttribute("aria-expanded", "true")
                                                                setAttribute("aria-controls", "panelsStayOpen-collapseOne")
                                                            }
                                                        }
                                                        div(className = "accordion-collapse collapse") {
                                                            setAttribute("id", "panelsStayOpen-collapseOne")
                                                            div(className = "accordion-body") {
                                                                add(trustedDevicesTable())
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    else -> {}
                                }
                            }.then {
                                window.setTimeout({
                                    window["feather"].replace()
                                }, 100)
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
            div {
                ObservableManager.setSubscriber("twoFaModalChangeListener") {
                    twoFaModalChangeListner.subscribe {
                        window.document.getElementById("twoFaCloseBtn")?.asDynamic()?.click()
                        window.setTimeout({
                            removeAll()
                            if(it) {
                                bootstrapModalComponent(
                                    modalId = "twoFa",
                                    title = "2FA Setup",
                                    contentBody = {
                                        return@bootstrapModalComponent div(className = "row") {
                                            div(className = "col-md-4") {
                                                setAttribute("id", "qrcode")
                                            }
                                            div(className = "col-md-8") {
                                                textInputComponent(
                                                    twoFaCodeInput
                                                )
                                                twoFaCodeInputButton.callback = {
                                                    sendTwoFaEnableRequest(twoFaCodeInput.getValue())
                                                }
                                                buttonComponent(twoFaCodeInputButton, "Submit")
                                            }
                                        }
                                    }
                                ) {

                                }
                            }
                        }, 100)
                    }
                }
            }
        }
    }

    return div {
        ObservableManager.setSubscriber("securitySettingsPage") {
            pageRefreshListner.subscribe {
                if(it) {
                    removeAll()
                    add(contentBody())
                    AppEngine.routing.updatePageLinks()
                    window.setTimeout({
                        window["feather"].replace()
                    }, 100)
                }
            }
        }
        pageRefreshListner.setState(true)
    }
}
