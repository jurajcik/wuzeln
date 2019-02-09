package at.wuzeln.manager.dto

import at.wuzeln.manager.dto.enums.MatchState
import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
open class MatchDto(
        val id: Long,
        val name: String,
        val createdDate: Long,
        val state: MatchState
)