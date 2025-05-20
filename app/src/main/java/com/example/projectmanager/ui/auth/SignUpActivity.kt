package com.example.projectmanager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivitySignUpBinding
import com.example.projectmanager.ui.main.MainActivity
import com.example.projectmanager.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        with(binding) {
            signUpButton.setOnClickListener {
                val displayName = displayNameInput.text.toString()
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                if (validateInput(displayName, email, password, confirmPassword)) {
                    viewModel.signUp(email, password, displayName)
                }
            }

            signInText.setOnClickListener {
                finish()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success<*> -> {
                        showLoading(false)
                        navigateToMain()
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun validateInput(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Validate display name
        if (displayName.isEmpty()) {
            binding.displayNameLayout.error = getString(R.string.invalid_display_name)
            isValid = false
        } else {
            binding.displayNameLayout.error = null
        }

        // Validate email
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        // Validate password
        if (password.isEmpty() || password.length < 6) {
            binding.passwordLayout.error = getString(R.string.invalid_password)
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        // Validate confirm password
        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.passwords_dont_match)
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.signUpButton.isEnabled = !show
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}