package com.capstone.tidurlelap.ui.result

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.databinding.ActivityResultBinding
import com.capstone.tidurlelap.ui.ViewModelFactory

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
                resultViewModel.getResult(token)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}