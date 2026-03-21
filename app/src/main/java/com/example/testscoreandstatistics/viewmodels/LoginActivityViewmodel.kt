package com.example.testscoreandstatistics.viewmodels

import androidx.lifecycle.ViewModel
import com.example.testscoreandstatistics.repositories.RestRepository

class LoginActivityViewmodel: ViewModel() {
    val restRepository = RestRepository()
    fun loginUser(username: String, password: String, listener: RestRepository.LoginListener){
        restRepository.loginUser(username, password, listener)
    }

}