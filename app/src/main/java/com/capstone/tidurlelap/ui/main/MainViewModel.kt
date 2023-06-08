package com.capstone.tidurlelap.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.capstone.tidurlelap.data.local.UserPreference
import com.capstone.tidurlelap.data.remote.model.UserModel

class MainViewModel(private val pref: UserPreference) : ViewModel() {
    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }
}