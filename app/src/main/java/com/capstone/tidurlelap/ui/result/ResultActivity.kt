package com.capstone.tidurlelap.ui.result

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.databinding.ActivityResultBinding
import com.capstone.tidurlelap.ui.ViewModelFactory
import com.capstone.tidurlelap.ui.main.MainActivity
import kotlin.math.round

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
class ResultActivity : AppCompatActivity() {
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
    }

    private fun setupViewModel() {
        resultViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[ResultViewModel::class.java]
    }

    private fun setupAction() {
        resultViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        resultViewModel.result.observe(this) {
            val time = convertSecondsToHours(it.sleepTime)
            binding.tvScore.text = it.sleepScore.toString()
            binding.sleephours.text = it.sleepTime.toString()
            binding.sleepnoise.text = it.sleepNoise.toString()
            binding.tvHours.text = getString(R.string.time_spent, time)

            when (it.sleepScore) {
                1 -> {
                    binding.result.text = getString(R.string.one_result)
                    binding.tvConclusion.text = getString(R.string.one_desc)
                }
                2 -> {
                    binding.result.text = getString(R.string.two_result)
                    binding.tvConclusion.text = getString(R.string.two_desc)
                }
                3 -> {
                    binding.result.text = getString(R.string.three_result)
                    binding.tvConclusion.text = getString(R.string.three_desc)
                }
                4 -> {
                    binding.result.text = getString(R.string.four_result)
                    binding.tvConclusion.text = getString(R.string.four_desc)
                }
                5 -> {
                    binding.result.text = getString(R.string.five_result)
                    binding.tvConclusion.text = getString(R.string.five_desc)
                }
                else -> {
                    binding.result.text = getString(R.string.failure)
                    binding.tvConclusion.text = getString(R.string.failure_desc)
                }
            }
        }

        resultViewModel.getUser().observe(this) {
            if (it.token.isNotEmpty()) {
                val token = it.token
                Log.d("Token", "Received token: $token")
                resultViewModel.getResult(token)
            }
        }


        binding.tvBackToScreen.setOnClickListener{
            startActivity(Intent(this@ResultActivity, MainActivity::class.java))
            finish()
        }

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

    fun convertSecondsToHours(seconds: Int): Double {
        val hours = seconds / 3600.0
        return hours
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}