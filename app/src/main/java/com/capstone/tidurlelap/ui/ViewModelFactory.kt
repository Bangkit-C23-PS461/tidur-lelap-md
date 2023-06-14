package com.capstone.tidurlelap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.ui.home.HomeViewModel
import com.capstone.tidurlelap.ui.login.LoginViewModel
import com.capstone.tidurlelap.ui.main.MainViewModel
import com.capstone.tidurlelap.ui.profile.ProfileViewModel
import com.capstone.tidurlelap.ui.result.ResultViewModel
import com.capstone.tidurlelap.ui.signup.SignupViewModel
import com.capstone.tidurlelap.ui.sleeptrack.SleepTrackViewModel

class ViewModelFactory(private val pref: UserPreference): ViewModelProvider.NewInstanceFactory() {
    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(pref: UserPreference): ViewModelFactory {
            if(INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(pref)
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }

    @Suppress
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(SignupViewModel::class.java) -> {
                SignupViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(pref) as T
            }
            modelClass.isAssignableFrom(SleepTrackViewModel::class.java) -> {
                SleepTrackViewModel(pref) as T
            }
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                ResultViewModel(pref) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}