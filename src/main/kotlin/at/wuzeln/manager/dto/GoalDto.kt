package at.wuzeln.manager.dto

import at.wuzeln.manager.model.enums.Position
import com.fasterxml.jackson.annotation.JsonClassDescription
import java.time.LocalDateTime

@JsonClassDescription
data class GoalDto(
        val id: Long,
        val date: LocalDateTime,
        val own: Boolean,
        val position: Position,
        val playerPersonId: Long,
        val goaliePersonId: Long
)