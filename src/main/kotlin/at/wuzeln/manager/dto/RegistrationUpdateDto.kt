package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class RegistrationUpdateDto(
        val name: String?,
        val persons: List<RegPersonDto>
) {
    @JsonClassDescription
    data class RegPersonDto(
            val id: Long,
            val register: Boolean
    )
}