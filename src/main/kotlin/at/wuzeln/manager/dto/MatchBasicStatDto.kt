package at.wuzeln.manager.dto

import at.wuzeln.manager.model.enums.TeamColor
import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class MatchBasicStatDto(
        val name: String,
        val createdDate: Long,
        val startDate: Long?,
        val endDate: Long?,
        val durationMillis: Long?,
        val winner: TeamColor?,
        val players: List<PlayerStatDto>,
        val idlePersons: List<Long>
) {
    data class PlayerStatDto(
            val personId: Long,
            val teamColor: TeamColor,
            val goals: Int,
            val goalsOwn: Int,
            val inGoalMillis: Long
    )
}