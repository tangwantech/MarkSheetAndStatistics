package com.example.testscoreandstatistics.repositories

import android.util.Log
import com.example.testscoreandstatistics.datamodels.StudentData
import com.example.testscoreandstatistics.datamodels.UserData
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RestRepository {

    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mediaType = "application/json;charset=utf-8".toMediaType()

    fun loginUser(username: String, password: String, deviceId: String, listener: LoginListener) {
        val params = mapOf(
            "username" to username,
            "password" to password,
            "deviceId" to deviceId
        )
        val url = "https://parseapi.back4app.com/functions/login"
        val requestBody = JSONObject(params).toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", CLIENT_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onLoginFailed(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val result = JSONObject(responseBody)["result"].toString()
                        UserRepository.updateUserData(result, username to password)
                        listener.onLoginSuccessful()
                    } catch (e: Exception) {
                        listener.onLoginFailed("Error parsing server response")
                    }
                } else {
                    val error = try {
                        val json = JSONObject(responseBody)
                        if (json.has("error")) json.getString("error")
                        else "Login failed: Invalid credentials"
                    } catch (e: Exception) {
                        "Login failed: Invalid credentials"
                    }
                    listener.onLoginFailed(error)
                }
            }
        })
    }

    fun logoutUser(username: String, password: String, listener: LogoutListener) {
        val params = mapOf(
            "username" to username,
            "password" to password
        )
        val url = "https://parseapi.back4app.com/functions/logout"
        val requestBody = JSONObject(params).toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", CLIENT_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onLogoutFailed(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    listener.onLogoutSuccessful()
                } else {
                    val responseBody = response.body?.string() ?: ""
                    val error = try {
                        JSONObject(responseBody).optString("error", "Logout failed")
                    } catch (e: Exception) {
                        "Logout failed"
                    }
                    listener.onLogoutFailed(error)
                }
            }
        })
    }

    fun fetchStudents(params: HashMap<String, String>, listener: GetStudentsListener){
        val url = "https://parseapi.back4app.com/functions/getStudents"
        val requestBody = JSONObject(params).toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", CLIENT_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onError(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().toString()
                    val result = JSONObject(responseBody)["result"].toString()
                    listener.onSuccess(result)
                }else{
                    listener.onError(response.body?.string().toString())
                }
            }
        })
    }

    fun saveStudents(params: Map<String, Any>, listener: SaveStudentsListener) {
        val url = "https://parseapi.back4app.com/functions/saveStudents"
        val requestBody = Gson().toJson(params).toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", CLIENT_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onError(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().toString()
                    val result = JSONObject(responseBody).optString("result", "Success")

                    listener.onSuccess(result)
                } else {

                    listener.onError(response.body?.string().toString())
                }
            }
        })
    }

    fun fetchStatistics(params: HashMap<String, String>, listener: GetStatisticsListener) {
        val url = "https://parseapi.back4app.com/functions/getStatistics"
        val requestBody = JSONObject(params).toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Parse-Application-Id", APPLICATION_ID)
            .addHeader("X-Parse-REST-API-Key", CLIENT_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onError(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string().toString()
//                    println(responseBody)
                    val result = JSONObject(responseBody)["result"].toString()
                    listener.onSuccess(result)
                } else {
                    listener.onError(response.body?.string().toString())
                }
            }
        })
    }

    interface LoginListener {
        fun onLoginSuccessful()
        fun onLoginFailed(error: String?)
    }

    interface LogoutListener {
        fun onLogoutSuccessful()
        fun onLogoutFailed(error: String?)
    }

    interface GetStudentsListener {
        fun onSuccess(result: String)
        fun onError(error: String?)
    }

    interface SaveStudentsListener {
        fun onSuccess(result: String)
        fun onError(error: String?)
    }

    interface GetStatisticsListener {
        fun onSuccess(result: String)
        fun onError(error: String?)
    }

    companion object {
        const val APPLICATION_ID = "gTb63sA4oOd0sUWpQoaHxFMPZCI1ZHpiYLUKFBzg"
        const val CLIENT_KEY = "M6p779nLtiLSz8dZUhEJUAPxYbUgrzCs6Egpernu"
    }
}
