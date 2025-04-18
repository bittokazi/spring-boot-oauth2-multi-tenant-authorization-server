package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

import io.kvision.core.Container
import io.kvision.core.onInput
import io.kvision.form.select.SelectInput
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.label

fun Container.selectInputComponent(
    formSelectInput: FormSelectInput,
    options: List<Pair<String, String>>? = listOf(),
    startValue: String? = null,
    disabledInput: Boolean = false
): Container {
    return div(className = "mb-3") {
        label(formSelectInput.label, className = "form-label")
        formSelectInput.input = SelectInput() {
            this.options = options
            startValue?.let { sv ->
                this.startValue = sv
            }
            disabled = disabledInput
        }

        formSelectInput.invalidFeedbackDiv = Div(className = "invalid-feedback")

        formSelectInput.input.onInput {
            formSelectInput.enforceValidation()
        }
        add(formSelectInput.input)
        add(formSelectInput.invalidFeedbackDiv)
    }
}

class FormSelectInput(
    val label: String = "[Default Label...]",
    val defaultInvalidFeedback: String? = "[Error...]",
    val options: List<Pair<String, String>>? = listOf(),
    val startValue: String? = null,
    val validator: (Int) -> Boolean
) : FormControl<SelectInput, String?> {

    lateinit var input: SelectInput
    lateinit var invalidFeedbackDiv: Div

    override fun setInput(input: SelectInput) {
        this.input = input
    }

    override fun getValue(): String? {
        return input.value
    }

    override fun getInput(): SelectInput {
        return input
    }

    override fun isValid(): Boolean {
        return validator(input.selectedIndex)
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
        when (validator(input.selectedIndex)) {
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
