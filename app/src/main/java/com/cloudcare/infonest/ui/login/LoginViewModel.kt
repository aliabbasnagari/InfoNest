package com.cloudcare.infonest.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.cloudcare.infonest.data.LoginRepository
import com.cloudcare.infonest.data.Result

import com.cloudcare.infonest.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginForm

    private val _loginResult = MutableStateFlow(LoginResult())
    val loginResult: StateFlow<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = loginRepository.login(email, password)
                if (result is Result.Success) {
                    _loginResult.value = LoginResult(
                        success = LoggedInUserView(
                            userId = result.data.userId,
                            email = result.data.email,
                            displayName = result.data.displayName
                        )
                    )
                } else {
                    _loginResult.value = LoginResult(
                        error = R.string.login_failed,
                        errorMessage = (result as? Result.Error)?.exception?.message
                    )
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult(
                    error = R.string.login_failed,
                    errorMessage = e.message
                )
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = loginRepository.register(email, password)
                if (result is Result.Success) {
                    _loginResult.value = LoginResult(
                        success = LoggedInUserView(
                            userId = result.data.userId,
                            email = result.data.email,
                            displayName = result.data.displayName
                        )
                    )
                } else {
                    _loginResult.value = LoginResult(
                        error = R.string.registration_failed,
                        errorMessage = (result as? Result.Error)?.exception?.message
                    )
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult(
                    error = R.string.registration_failed,
                    errorMessage = e.message
                )
            }
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}