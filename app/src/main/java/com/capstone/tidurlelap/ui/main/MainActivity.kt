package com.capstone.tidurlelap.ui.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.capstone.tidurlelap.R
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.databinding.ActivityMain2Binding
import com.capstone.tidurlelap.ui.ViewModelFactory
import com.capstone.tidurlelap.ui.welcome.WelcomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")
class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()

        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_sleeptrack, R.id.navigation_profile
            )
        )

        mainViewModel.getUser().observe(this) { user ->
            if (user.isLogin && !user.token.isEmpty()) {
                setupActionBarWithNavController(navController, appBarConfiguration)
                navView.setupWithNavController(navController)
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
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

}