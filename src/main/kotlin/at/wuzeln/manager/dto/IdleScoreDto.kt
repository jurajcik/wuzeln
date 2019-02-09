package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class IdleScoreDto(
        val id: Long,
        val score: Double
        ) {
}