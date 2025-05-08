package com.cloudcare.infonest.presentation.activity

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cloudcare.infonest.databinding.ActivityLoginBinding

import com.cloudcare.infonest.R
import com.cloudcare.infonest.ui.login.LoggedInUserView
import com.cloudcare.infonest.ui.login.LoginViewModel
import com.cloudcare.infonest.ui.login.LoginViewModelFactory
import com.google.android.material.color.DynamicColors
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        auth = Firebase.auth
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val cbAccount = binding.cbAccount

        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(auth))[LoginViewModel::class.java]

        lifecycleScope.launch {
            loginViewModel.loginFormState.collectLatest { loginState ->
                login.isEnabled = loginState.isDataValid
                loginState.emailError?.let { email.error = getString(it) }
                loginState.passwordError?.let { password.error = getString(it) }
            }
        }

        lifecycleScope.launch {
            loginViewModel.loginResult.collectLatest { loginResult ->
                loading.visibility = View.GONE
                loginResult.let {
                    if (it.error != null) {
                        val message = it.errorMessage ?: getString(it.error)
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                    it.success?.let { user ->
                        updateUiWithUser(user)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }

        cbAccount!!.setOnCheckedChangeListener { _, isChecked ->
            login.text =
                if (isChecked)
                    getString(R.string.action_sign_up_short)
                else
                    getString(R.string.action_sign_in_short)
        }

        email.afterTextChanged {
            loginViewModel.loginDataChanged(email.text.toString(), password.text.toString())
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(email.text.toString(), password.text.toString())
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE && login.isEnabled) {
                    loading.visibility = View.VISIBLE
                    loginViewModel.login(email.text.toString(), password.text.toString())
                }
                false
            }
        }

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            if (cbAccount.isChecked) {
                loginViewModel.register(emailText, passwordText)
                Log.d("TAG", "Attempting registration")
            } else {
                loginViewModel.login(emailText, passwordText)
                Log.d("TAG", "Attempting login")
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        Toast.makeText(applicationContext, "$welcome $displayName", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}