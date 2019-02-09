package at.wuzeln.manager.dto

import at.wuzeln.manager.model.enums.MatchCreationMethod
import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class MatchCreationDto(
        val name: String,
        val teamBlue: List<Long>,
        val teamRed: List<Long>,
        val idlePersons: Set<Long>,
        val method: MatchCreationMethod,
        val registrationId: Long?
)