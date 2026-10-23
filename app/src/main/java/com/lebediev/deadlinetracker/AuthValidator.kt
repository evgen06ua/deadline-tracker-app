package com.lebediev.deadlinetracker

object AuthValidator {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !emailRegex.matches(email.trim()) -> "Enter a valid email"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < 6 -> "Password must have at least 6 characters"
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirm: String): String? =
        if (password != confirm) "Passwords do not match" else null
}