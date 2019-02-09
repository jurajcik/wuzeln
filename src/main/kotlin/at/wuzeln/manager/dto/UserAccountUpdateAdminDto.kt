package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class UserAccountUpdateAdminDto(
        val registered: Boolean,
        val admin: Boolean
)