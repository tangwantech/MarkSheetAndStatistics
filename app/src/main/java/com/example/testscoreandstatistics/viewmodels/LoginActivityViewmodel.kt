package com.example.testscoreandstatistics.viewmodels

import androidx.lifecycle.ViewModel
import com.example.testscoreandstatistics.repositories.RestRepository

class LoginActivityViewmodel: ViewModel() {
    private val restRepository = RestRepository()
    
    fun loginUser(username: String, password: String, deviceId: String, listener: RestRepository.LoginListener){
        restRepository.loginUser(username, password, deviceId, listener)
    }

}
