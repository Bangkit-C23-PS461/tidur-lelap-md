package com.capstone.tidurlelap.ui.signup

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.databinding.ActivitySignUpBinding
import com.capstone.tidurlelap.ui.ViewModelFactory
import com.capstone.tidurlelap.ui.login.LoginActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var signupViewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetController = window.insetsController
            insetController?.let {
                it.hide(WindowInsets.Type.statusBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupViewModel() {
        signupViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[SignupViewModel::class.java]
    }

    private fun setupAction() {
        signupViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            binding.etPassword.error = if (text.toString().length < 8) {
                "Password must contain at least 8 characters"
            } else {
                null
            }
        }

        binding.buttonSignup.setOnClickListener {
            val name = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            when {
                name.isEmpty() -> {
                    binding.etUsername.error = "Input your username"
                }
                email.isEmpty() -> {
                    binding.etEmail.error = "Input your email"
                }
                password.isEmpty() -> {
                    binding.etPassword.error = "Input your password"
                }
                else -> {
                    signupViewModel.userRegister(name, email, password)
                    signupViewModel.isRegistrationSuccessful.observe(this) {
                        if (it) {
                            AlertDialog.Builder(this).apply {
                                setTitle("Success")
                                setMessage(
                                    "Your account has been successfully created"
                                )
                                setPositiveButton("Continue") { _, _ ->
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                create()
                                show()
                            }
                        }
                        else {
                            AlertDialog.Builder(this).apply {
                                setTitle("Error!")
                                setMessage("Registration failed")
                                setPositiveButton("Retry") { _,_ ->
                                }
                                create()
                                show()
                            }
                        }
                        }
                    }
            }
        }

        binding.buttonSignin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        }
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
