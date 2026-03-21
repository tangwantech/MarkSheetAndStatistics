package com.example.testscoreandstatistics.datamodels


data class StudentsData(val students: List<StudentData>)
data class StudentData(val studentName: String, val studentRegID: String, val gender: String, val sequenceName: String, var score: Double, var isRegistered: Boolean)


