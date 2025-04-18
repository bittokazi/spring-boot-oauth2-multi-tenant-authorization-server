package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

import io.kvision.core.Container
import io.kvision.html.Button
import io.kvision.html.ButtonType
import io.kvision.html.div
import io.kvision.html.span

fun Container.buttonComponent(
    buttonInput: FormButton,
    value: String = ""
): Container {
    return div(className = "d-grid gap-2 mt-3") {
        buttonInput.input = Button(
            value,
            type = ButtonType.SUBMIT,
            className = "btn btn-lg btn-primary"
        ) {
            if (buttonInput.callback != null) {
                onClick {
                    buttonInput.callback!!.invoke()
                }
            }
        }

        buttonInput.loadingInput = Button(
            "",
            type = ButtonType.SUBMIT,
            className = "btn btn-lg btn-primary"
        ) {
            disabled = true
            span(className = "spinner-border spinner-border-sm") {
                setAttribute("role", "status")
                setAttribute("aria-hidden", "true")
            }
            span(className = "sr-only", content = "Loading...")
        }

        add(buttonInput.input)
        add(buttonInput.loadingInput)

        buttonInput.loadingInput.hide()
    }
}

class FormButton(var callback: (() -> Unit)? = null) : FormControl<Button, Unit> {

    lateinit var input: Button
    lateinit var loadingInput: Button

    fun showLoading() {
        input.hide()
        loadingInput.show()
    }

    fun resetInput() {
        input.show()
        loadingInput.hide()
    }

    override fun setInput(input: Button) {
        this.input = input
    }

    override fun getValue(): Unit {
        return
    }

    override fun getInput(): Button {
        return input
    }

    override fun isValid(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setCustomError(message: String) {
        TODO("Not yet implemented")
    }

    override fun enforceValidation() {
        TODO("Not yet implemented")
    }
}
