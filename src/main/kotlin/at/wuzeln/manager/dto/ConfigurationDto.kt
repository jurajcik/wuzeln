package at.wuzeln.manager.dto

import com.fasterxml.jackson.annotation.JsonClassDescription
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@JsonClassDescription
@Component
data class ConfigurationDto(
        @Value("\${oauth.clientId}")
        val oauthClientId: String,

        @Value("\${validation.username.pattern}")
        val validationUsernamePattern: String,

        @Value("\${validation.username.text}")
        val validationUsernameText: String,

        @Value("\${registration.proposal_calculation.months}")
        val proposalCalculationMonths: Int,

        @Value("\${grafana.link.dashboard}")
        val grafanaLinkDashboard: String,

        @Value("\${grafana.link.dashboard-individual}")
        val grafanaLinkDashboardIndividual: String
)