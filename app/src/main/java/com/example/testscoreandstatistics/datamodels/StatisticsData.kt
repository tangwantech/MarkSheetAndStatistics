package com.example.testscoreandstatistics.datamodels

/**
 * Data classes for parsing school statistics JSON.
 * Uses Maps for dynamic keys like Class names, Subjects, and Sequences.
 */

data class StatisticsResponse(
    val statistics: Map<String, MainClassData>
)

data class MainClassData(
    val overallStatistics: Map<String, Map<String, SequenceStats>>, // Subject -> Sequence -> SequenceStats
    val subclasses: Map<String, Map<String, Map<String, SequenceStats>>> // Subclass -> Subject -> Sequence -> SequenceStats
)

data class SequenceStats(
    val males: GenderStats,
    val females: GenderStats,
    val overall: GenderStats
)

data class GenderStats(
    val numSat: Int,
    val numPassed: Int,
    val percentagePassed: Double
)
