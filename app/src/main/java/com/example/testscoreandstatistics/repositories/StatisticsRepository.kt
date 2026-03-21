package com.example.testscoreandstatistics.repositories

import com.example.testscoreandstatistics.datamodels.StatisticsResponse
import com.google.gson.Gson

class StatisticsRepository {
    companion object {
        fun fetchStatistics(params: HashMap<String, String>, listener: GetStatisticsListener) {
            val restRepository = RestRepository()
            restRepository.fetchStatistics(params, object : RestRepository.GetStatisticsListener {
                override fun onSuccess(result: String) {
                    try {
//                        println(result)
                        val statisticsResponse = Gson().fromJson(result, StatisticsResponse::class.java)
//                        println(statisticsResponse)
                        listener.onSuccess(statisticsResponse)
                    } catch (e: Exception) {
                        listener.onError(e.message)
                    }
                }

                override fun onError(error: String?) {
                    listener.onError(error)
                }
            })
        }
    }

    interface GetStatisticsListener {
        fun onSuccess(result: StatisticsResponse)
        fun onError(error: String?)
    }
}
