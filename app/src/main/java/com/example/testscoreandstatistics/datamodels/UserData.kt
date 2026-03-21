package com.example.testscoreandstatistics.datamodels

data class UserData(val sessionToken: String?, val isOnline: Boolean, val fullName: String, val subjectsTaught: HashMap<String, HashMap<String, List<String>>>)

