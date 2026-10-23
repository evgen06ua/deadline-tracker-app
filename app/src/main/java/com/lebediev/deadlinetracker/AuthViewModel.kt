package com.lebediev.deadlinetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = repo.isLoggedInFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, repo.isLoggedIn())

    fun currentEmail() = repo.currentEmail()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun onConfirmChange(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmError = null) }

    fun clearForm() {
        _uiState.value = AuthUiState()
    }

    fun login() {
        val s = _uiState.value
        val emailError = AuthValidator.validateEmail(s.email)
        val passwordError = AuthValidator.validatePassword(s.password)
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError, generalError = null)
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            val result = repo.login(s.email.trim(), s.password)
            finish(result)
        }
    }

    fun register() {
        val s = _uiState.value
        val emailError = AuthValidator.validateEmail(s.email)
        val passwordError = AuthValidator.validatePassword(s.password)
        val confirmError = AuthValidator.validatePasswordConfirmation(s.password, s.confirmPassword)
        if (emailError != null || passwordError != null || confirmError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmError = confirmError,
                    generalError = null
                )
            }
            return
        }
        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            val result = repo.register(s.email.trim(), s.password)
            finish(result)
        }
    }

    fun logout() = repo.logout()

    private fun finish(result: Result<Unit>) {
        if (result.isSuccess) {
            _uiState.value = AuthUiState()
        } else {
            val message = friendlyMessage(result.exceptionOrNull())
            _uiState.update { it.copy(isLoading = false, generalError = message) }
        }
    }

    private fun friendlyMessage(e: Throwable?): String = when (e) {
        is FirebaseAuthUserCollisionException -> "An account with this email already exists"
        is FirebaseAuthWeakPasswordException -> "Password is too weak"
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
        is FirebaseAuthInvalidUserException -> "Incorrect email or password"
        is FirebaseNetworkException -> "No internet connection"
        else -> "Something went wrong. Please try again"
    }
}