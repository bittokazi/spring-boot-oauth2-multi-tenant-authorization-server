package com.bittokazi.oauth2.auth.frontend.frontend.base.components.form

interface FormControl<T, E> {
    fun setInput(input: T)
    fun getInput(): T
    fun isValid(): Boolean
    fun setCustomError(message: String)
    fun enforceValidation()
    fun getValue(): E
}
