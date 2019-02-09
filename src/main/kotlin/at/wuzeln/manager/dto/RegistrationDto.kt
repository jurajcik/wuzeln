package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class RegistrationDto(
        val id: Long,
        val name: String,
        val createdDate: Long,
        val isOpened: Boolean,
        val persons: List<Long>
)