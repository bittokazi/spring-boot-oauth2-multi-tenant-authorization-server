package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

import io.kvision.core.Container
import io.kvision.core.onInput
import io.kvision.form.text.TextInput
import io.kvision.html.Div
import io.kvision.html.InputType
import io.kvision.html.div
import io.kvision.html.label

fun Container.textInputComponent(
    formTextInput: FormTextInput,
    value: String = "",
    disabledInput: Boolean = false,
    inputType: InputType = InputType.TEXT
): Container {
    return div(className = "mb-3") {
        label(formTextInput.label, className = "form-label")
        formTextInput.input = TextInput(
            inputType,
            className = "form-control form-control-lg",
            value = value
        ) {
            disabled = disabledInput
            placeholder = formTextInput.placeholder
        }

        formTextInput.invalidFeedbackDiv = Div(className = "invalid-feedback")

        formTextInput.input.onInput {
            formTextInput.enforceValidation()
        }
        add(formTextInput.input)
        add(formTextInput.invalidFeedbackDiv)
    }
}

class FormTextInput(
    val label: String = "[Default Label...]",
    val placeholder: String = "[Default Placeholder...]",
    val defaultInvalidFeedback: String? = "[Error...]",
    val value: String = "",
    val validator: (String?) -> Boolean
) : FormControl<TextInput, String> {

    lateinit var input: TextInput
    lateinit var invalidFeedbackDiv: Div

    override fun getValue(): String {
        return input.value ?: ""
    }

    override fun setInput(input: TextInput) {
        this.input = input
    }

    override fun getInput(): TextInput {
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
