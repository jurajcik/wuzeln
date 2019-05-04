package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class UserAccountDto(
        val username: String?,
        val googleAccountId: String?,
        val registered: Boolean,
        val admin: Boolean,
        val active: Boolean,
        val createdDate: Long?
)
