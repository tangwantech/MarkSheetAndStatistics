package com.example.testscoreandstatistics.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testscoreandstatistics.datamodels.StudentData
import com.example.testscoreandstatistics.datamodels.StatisticsResponse
import com.example.testscoreandstatistics.repositories.StudentsRepository
import com.example.testscoreandstatistics.repositories.StatisticsRepository
import com.example.testscoreandstatistics.repositories.UserRepository
import com.example.testscoreandstatistics.repositories.RestRepository

class MainActivityViewModel: ViewModel() {

    private val _students = MutableLiveData<List<StudentData>?>()
    val students: LiveData<List<StudentData>?> = _students

    private val _statistics = MutableLiveData<StatisticsResponse?>()
    val statistics: LiveData<StatisticsResponse?> = _statistics

    private val _marksheetSelectionParams = MutableLiveData<HashMap<String, String>?>()
    val marksheetSelectionParams: LiveData<HashMap<String, String>?> = _marksheetSelectionParams

    private val _statisticsSelectionParams = MutableLiveData<HashMap<String, String>?>()
    val statisticsSelectionParams: LiveData<HashMap<String, String>?> = _statisticsSelectionParams

    private var originalStudentsSize = 0

    fun getSubjectsTaught(): List<String> {
        return UserRepository.getSubjectsTaught().sorted()
    }

    fun getMainClassesForSubject(subjectName: String): List<String> {
        return UserRepository.getMainClassesForSubject(subjectName).sorted()
    }

    fun getSubclassesForMainClass(subjectName: String, mainClassName: String): List<String> {
        return UserRepository.getSubclassesForMainClass(subjectName, mainClassName).sorted()
    }

    fun fetchStudents(params: HashMap<String, String>, listener: StudentsRepository.FetchStudentsListener) {
        _marksheetSelectionParams.value = params
        StudentsRepository.fetchStudents(params, object : StudentsRepository.FetchStudentsListener {
            override fun onSuccess(result: List<StudentData>) {
                originalStudentsSize = result.size
                _students.postValue(result)
                listener.onSuccess(result)
            }

            override fun onError(error: String?) {
                listener.onError(error)
            }
        })
    }

    fun fetchStatistics(params: HashMap<String, String>, listener: StatisticsRepository.GetStatisticsListener) {
        _statisticsSelectionParams.value = params
        
        // Remove subclass from parameters sent to the server for statistics
        val requestParams = HashMap(params)
        requestParams.remove("subclass")
        
        StatisticsRepository.fetchStatistics(requestParams, object : StatisticsRepository.GetStatisticsListener {
            override fun onSuccess(result: StatisticsResponse) {
                _statistics.postValue(result)
                listener.onSuccess(result)
            }

            override fun onError(error: String?) {
                listener.onError(error)
            }
        })
    }

    fun saveStudents(listener: StudentsRepository.SaveStudentsListener) {
        val currentStudents = _students.value
        val params = _marksheetSelectionParams.value
        
        if (currentStudents != null && params != null) {
            if (currentStudents.size != originalStudentsSize) {
                listener.onError("Student list size mismatch: original size $originalStudentsSize, current size ${currentStudents.size}")
                return
            }

            val saveParams = LinkedHashMap<String, Any>()
            saveParams["sessionToken"] = UserRepository.getSessionToken() ?: ""
            saveParams["mainClass"] = params["mainClass"] ?: ""
            saveParams["subclass"] = params["subclass"] ?: ""
            saveParams["subject"] = params["subject"] ?: ""
            saveParams["sequence"] = params["sequence"] ?: ""
            saveParams["studentsFromUser"] = currentStudents
            
            StudentsRepository.saveStudents(saveParams, listener)
        } else {
            listener.onError("No students or selection parameters found to save")
        }
    }

    fun logoutUser(listener: RestRepository.LogoutListener) {
        val credentials = UserRepository.getUserCredentials()
        if (credentials != null) {
            val restRepository = RestRepository()
            restRepository.logoutUser(credentials.first, credentials.second, listener)
        } else {
            listener.onLogoutFailed("No user credentials found")
        }
    }
}
