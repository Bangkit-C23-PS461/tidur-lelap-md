package com.capstone.tidurlelap.ui.welcome

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.capstone.tidurlelap.databinding.ActivityWelcomeBinding
import com.capstone.tidurlelap.ui.login.LoginActivity
import com.capstone.tidurlelap.ui.signup.SignUpActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
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

    private fun setupAction() {
        binding.apply {
            signupButton.setOnClickListener {
                val intent = Intent(this@WelcomeActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
            loginButton.setOnClickListener {
                val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
                startActivity(intent)

            }
        }
    }
}