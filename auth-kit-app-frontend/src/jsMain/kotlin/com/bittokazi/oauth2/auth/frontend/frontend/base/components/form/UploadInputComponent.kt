package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

import io.kvision.core.Component
import io.kvision.core.Container
import io.kvision.core.onInput
import io.kvision.form.upload.UploadInput
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.label
import io.kvision.types.KFile

fun Container.uploadInputComponent(
    formUploadInput: FormUploadInput,
    middleContentFn: (() -> Component?)? = null,
    disabledInput: Boolean = false
): Container {
    return div(className = "mb-3") {
        label(formUploadInput.label, className = "form-label")

        val middleContent = middleContentFn?.invoke()
        middleContent?.let {
            add(it)
        }

        formUploadInput.input = UploadInput() {
            multiple = formUploadInput.multiple
            accept = formUploadInput.accept
            disabled = disabledInput
        }

        formUploadInput.invalidFeedbackDiv = Div(className = "invalid-feedback")

        formUploadInput.input.onInput {
            formUploadInput.enforceValidation()
        }
        add(formUploadInput.input)
        add(formUploadInput.invalidFeedbackDiv)
    }
}

class FormUploadInput(
    val label: String = "[Default Label...]",
    val defaultInvalidFeedback: String? = "[Error...]",
    val value: String = "",
    val multiple: Boolean = false,
    val accept: List<String> = listOf("image/*"),
    val validator: (UploadInput) -> Boolean
) : FormControl<UploadInput, List<KFile>?> {

    lateinit var input: UploadInput
    lateinit var invalidFeedbackDiv: Div

    override fun setInput(input: UploadInput) {
        this.input = input
    }

    override fun getValue(): List<KFile>? {
        return input.value
    }

    override fun getInput(): UploadInput {
        return input
    }

    override fun isValid(): Boolean {
        return validator(input)
    }

    override fun setCustomError(message: String) {
        if (!input.hasCssClass("is-invalid")) {
            input.addCssClass("is-invalid")
        }
        if (input.hasCssClass("is-valid")) {
            input.removeCssClass("is-valid")
        }
        invalidFeedbackDiv.content = message
    }

    override fun enforceValidation() {
        when (validator(input)) {
            true -> {
                if (!input.hasCssClass("is-valid")) {
                    input.addCssClass("is-valid")
                }
                if (input.hasCssClass("is-invalid")) {
                    input.removeCssClass("is-invalid")
                }
            }

            false -> {
                if (!input.hasCssClass("is-invalid")) {
                    input.addCssClass("is-invalid")
                }
                if (input.hasCssClass("is-valid")) {
                    input.removeCssClass("is-valid")
                }
                invalidFeedbackDiv.content = defaultInvalidFeedback
            }
        }
    }
}
