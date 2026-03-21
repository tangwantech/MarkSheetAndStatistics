package com.example.testscoreandstatistics.repositories

import com.example.testscoreandstatistics.datamodels.StudentData
import com.example.testscoreandstatistics.datamodels.StudentsData
import com.google.gson.Gson

class StudentsRepository {
    companion object {
        private var students: StudentsData? = null

        fun fetchStudents(params: HashMap<String, String>, listener: FetchStudentsListener){
            val restRepository = RestRepository()
            restRepository.fetchStudents(params, object: RestRepository.GetStudentsListener{
                override fun onSuccess(result: String) {
                    students = Gson().fromJson(result, StudentsData::class.java)
                    listener.onSuccess(students!!.students)
                }

                override fun onError(error: String?) {
                    listener.onError(error)
                }
            })
        }

        fun saveStudents(params: Map<String, Any>, listener: SaveStudentsListener) {
            val restRepository = RestRepository()
            restRepository.saveStudents(params, object : RestRepository.SaveStudentsListener {
                override fun onSuccess(result: String) {
                    listener.onSuccess(result)
                }

                override fun onError(error: String?) {
                    listener.onError(error)
                }
            })
        }
    }
    interface FetchStudentsListener {
        fun onSuccess(result: List<StudentData>)
        fun onError(error: String?)
    }

    interface SaveStudentsListener {
        fun onSuccess(result: String)
        fun onError(error: String?)
    }

}