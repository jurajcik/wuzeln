package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonClassDescription
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserAccountUpdateAdminDto(
        val registered: Boolean,
        val admin: Boolean,
        val active: Boolean
)
