package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

import io.kvision.core.Container
import io.kvision.core.onInput
import io.kvision.form.check.CheckBoxInput
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.label

fun Container.switchInputComponent(
    formInput: FormSwitchInput,
    value: Boolean = false,
    disabledInput: Boolean = false
): Container {
    return div(className = "mb-3") {
        div(className = "form-check form-switch") {
            formInput.input = CheckBoxInput(
                className = "form-check-input",
                value = value
            ) {
                disabled = disabledInput
            }

            formInput.invalidFeedbackDiv = Div(className = "invalid-feedback")

            formInput.input.onInput {
                formInput.enforceValidation()
            }
            add(formInput.input)
            add(formInput.invalidFeedbackDiv)
            label(formInput.label, className = "form-check-label")
        }
    }
}

class FormSwitchInput(
    val label: String = "[Default Label...]",
    val defaultInvalidFeedback: String? = "[Error...]",
    val value: Boolean = false,
    val validator: (Boolean?) -> Boolean
) : FormControl<CheckBoxInput, Boolean> {

    lateinit var input: CheckBoxInput
    lateinit var invalidFeedbackDiv: Div

    override fun setInput(input: CheckBoxInput) {
        this.input = input
    }

    override fun getValue(): Boolean {
        return input.value
    }

    override fun getInput(): CheckBoxInput {
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
