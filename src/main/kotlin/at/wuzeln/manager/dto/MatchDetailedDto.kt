package at.wuzeln.manager.dto

import at.wuzeln.manager.dto.enums.MatchState
import at.wuzeln.manager.model.enums.Position
import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
class MatchDetailedDto(
        id: Long,
        name: String,
        createdDate: Long,
        state: MatchState,
        val blueTeam: TeamDto,
        val redTeam: TeamDto,
        val lastGoal: GoalDto?
) : MatchDto(id, name, createdDate, state) {

    @JsonClassDescription
    data class TeamDto(
            val players: List<PlayerDto>,
            val goals: Int
    )

    @JsonClassDescription
    data class PlayerDto(
            val personId: Long,
            val currentPosition: Position
    )

}