package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseSeeder
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val profileMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.ensureDemoSeeded()
            _uiState.update { it.copy(isReady = true) }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, generalError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
        }
    }

    fun fillDemoCredentials() {
        _uiState.update {
            it.copy(
                email = DatabaseSeeder.DEMO_EMAIL,
                password = DatabaseSeeder.DEMO_PASSWORD,
                emailError = null,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun clearErrors() {
        _uiState.update {
            it.copy(
                emailError = null,
                passwordError = null,
                nameError = null,
                confirmPasswordError = null,
                generalError = null,
                profileMessage = null
            )
        }
    }

    fun login() {
        val state = _uiState.value
        val emailErr = ValidationUtils.validateEmail(state.email)
        val passErr = if (state.password.isEmpty()) "Le mot de passe est obligatoire" else null

        if (emailErr != null || passErr != null) {
            _uiState.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = authRepository.login(state.email, state.password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        AuthUiState(isReady = true)
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, generalError = result.message)
                    }
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        val nameErr = ValidationUtils.validateName(state.name)
        val emailErr = ValidationUtils.validateEmail(state.email)
        val passErr = ValidationUtils.validatePassword(state.password)
        val confirmErr = if (state.confirmPassword != state.password) {
            "Les mots de passe ne correspondent pas"
        } else null

        if (nameErr != null || emailErr != null || passErr != null || confirmErr != null) {
            _uiState.update {
                it.copy(
                    nameError = nameErr,
                    emailError = emailErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            when (val result = authRepository.register(state.name, state.email, state.password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        AuthUiState(isReady = true)
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, generalError = result.message)
                    }
                }
            }
        }
    }

    fun updateProfileName(userId: Long, newName: String, onDone: (Boolean, String?) -> Unit) {
        val err = ValidationUtils.validateName(newName)
        if (err != null) {
            onDone(false, err)
            return
        }
        viewModelScope.launch {
            authRepository.updateProfileName(userId, newName)
            onDone(true, null)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthUiState(isReady = true) }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
    }
}
