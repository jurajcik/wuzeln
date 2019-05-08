package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class PersonalScoreDto(
        val id: Long,
        val matchesPlayed: Int,
        val matchesWon: Int,
        val goalsSum: Int,
        val goalsOwnSum: Int,
        val timeInGoalMillisSum: Int,
        val scoreOffensive: Double,
        val scoreDefensive: Double,
        val scoreVictory: Double
) {
    var scoreOffensiveNormalized: Double? = null
    var scoreDefensiveNormalized: Double? = null
    var scoreVictoryNormalized: Double? = null
    var scoreNormalized: Double? = null

}
