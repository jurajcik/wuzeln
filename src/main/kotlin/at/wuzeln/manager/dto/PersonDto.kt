package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription
data class PersonDto(
        val id: Long,
        val name: String
        // TODO add username
        //val username: String
)