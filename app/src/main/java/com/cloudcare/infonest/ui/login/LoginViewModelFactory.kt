package com.cloudcare.infonest.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cloudcare.infonest.data.LoginDataSource
import com.cloudcare.infonest.data.LoginRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory (private val auth: FirebaseAuth) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                loginRepository = LoginRepository(
                    dataSource = LoginDataSource(auth)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}