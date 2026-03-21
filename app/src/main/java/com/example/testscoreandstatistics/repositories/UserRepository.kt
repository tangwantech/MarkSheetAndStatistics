package com.example.testscoreandstatistics.repositories

import com.example.testscoreandstatistics.datamodels.UserData
import com.google.gson.Gson

class UserRepository {
    companion object{
        private var userData: UserData? = null
        
        fun updateUserData(result: String) {
            userData = Gson().fromJson(result, UserData::class.java)
        }

        fun getSubjectsTaught(): List<String> {
            return userData?.subjectsTaught?.keys?.toList() ?: emptyList()
        }

        fun getMainClassesForSubject(subjectName: String): List<String> {
            return userData?.subjectsTaught?.get(subjectName)?.keys?.toList() ?: emptyList()
        }

        fun getSubclassesForMainClass(subjectName: String, mainClassName: String): List<String> {
            return userData?.subjectsTaught?.get(subjectName)?.get(mainClassName)?.toList() ?: emptyList()
        }

        fun getSessionToken(): String? {
            return userData?.sessionToken
        }

        fun getUserFullName(): String {
            return userData?.fullName ?: ""
        }

        fun clearUserData() {
            userData = null
        }
    }
}
