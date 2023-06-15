package com.capstone.tidurlelap.ui.result

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.databinding.ActivityResultBinding
import com.capstone.tidurlelap.ui.ViewModelFactory
import com.capstone.tidurlelap.ui.home.HomeFragment
import com.capstone.tidurlelap.ui.login.LoginActivity
import com.capstone.tidurlelap.ui.main.MainActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
class ResultActivity : AppCompatActivity() {
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
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
            binding.tvScore.text = it.sleepScore.toString()
            binding.sleephours.text = it.sleepTime.toString()
            binding.sleepnoise.text = it.sleepNoise.toString()

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

//        val navController = findNavController(R.id.nav_host_fragment_activity_main2)

        binding.tvBackToScreen.setOnClickListener{
//            navController.navigateUp()
//            navController.navigate(R.id.navigation_home)
            startActivity(Intent(this@ResultActivity, MainActivity::class.java))
            finish()
        }

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}