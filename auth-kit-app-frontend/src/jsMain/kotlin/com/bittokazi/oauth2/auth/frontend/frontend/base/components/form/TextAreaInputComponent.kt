package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

import io.kvision.core.Container
import io.kvision.core.onInput
import io.kvision.form.text.TextAreaInput
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.label

fun Container.textAreaInputComponent(
    formTextAreaInput: FormTextAreaInput,
    value: String = "",
    disabledInput: Boolean = false
): Container {
    return div(className = "mb-3") {
        label(formTextAreaInput.label, className = "form-label")
        formTextAreaInput.input = TextAreaInput(value = value) {
            disabled = disabledInput
        }

        formTextAreaInput.invalidFeedbackDiv = Div(className = "invalid-feedback")

        formTextAreaInput.input.onInput {
            formTextAreaInput.enforceValidation()
        }
        add(formTextAreaInput.input)
        add(formTextAreaInput.invalidFeedbackDiv)
    }
}

class FormTextAreaInput(
    val label: String = "[Default Label...]",
    val defaultInvalidFeedback: String? = "[Error...]",
    val value: String = "",
    val validator: (String?) -> Boolean
) : FormControl<TextAreaInput, String> {

    lateinit var input: TextAreaInput
    lateinit var invalidFeedbackDiv: Div

    override fun setInput(input: TextAreaInput) {
        this.input = input
    }

    override fun getValue(): String {
        return input.value ?: ""
    }

    override fun getInput(): TextAreaInput {
        return input
    }

    override fun isValid(): Boolean {
        return validator(input.value)
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
        when (validator(input.value)) {
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
