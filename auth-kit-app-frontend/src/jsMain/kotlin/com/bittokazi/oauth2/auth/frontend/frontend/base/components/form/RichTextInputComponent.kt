package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

import io.kvision.core.Container
import io.kvision.core.onChange
import io.kvision.form.text.RichTextInput
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.label

fun Container.richTextInputComponent(
    formRichTextInput: FormRichTextInput,
    value: String = "",
    disabledInput: Boolean = false
): Container {
    return div(className = "mb-3") {
        label(formRichTextInput.label, className = "form-label")
        formRichTextInput.input = RichTextInput(value = value) {
            disabled = disabledInput
        }

        formRichTextInput.invalidFeedbackDiv = Div(className = "invalid-feedback")

        formRichTextInput.input.onChange {
            formRichTextInput.enforceValidation()
        }
        add(formRichTextInput.input)
        add(formRichTextInput.invalidFeedbackDiv)
    }
}

class FormRichTextInput(
    val label: String = "[Default Label...]",
    val defaultInvalidFeedback: String? = "[Error...]",
    val value: String = "",
    val validator: (String?) -> Boolean
) : FormControl<RichTextInput, String> {

    lateinit var input: RichTextInput
    lateinit var invalidFeedbackDiv: Div

    override fun setInput(input: RichTextInput) {
        this.input = input
    }

    override fun getValue(): String {
        return input.value ?: ""
    }

    override fun getInput(): RichTextInput {
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
