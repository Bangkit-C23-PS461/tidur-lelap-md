package com.capstone.tidurlelap.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.databinding.ActivityMainBinding
import com.capstone.tidurlelap.ui.ViewModelFactory
import com.capstone.tidurlelap.ui.welcome.WelcomeActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupViewModel()
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                binding.textView.text = "Hi, {user.name}"
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
        }
    }
}