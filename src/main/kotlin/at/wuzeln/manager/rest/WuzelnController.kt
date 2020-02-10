package at.wuzeln.manager.rest

import at.wuzeln.manager.dto.*
import at.wuzeln.manager.model.enums.MatchCreationMethod
import at.wuzeln.manager.service.*
import mu.KotlinLogging
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
class WuzelnController(
        private val personService: PersonService,
        private val matchService: MatchService,
        private val statsService: StatsService,
        private val registrationService: RegistrationService,
        private val calculationService: CalculationService,
        private val configuration: ConfigurationDto
) {

    private val log = KotlinLogging.logger {}

    @GET
    @Path("/configuration")
    fun getConfiguration(): ConfigurationDto {
        return configuration
    }

    @GET
    @Path("/persons")
    fun findAllPersons(): List<PersonDto> {
        // TODO SEC: where caller is in groups

        return personService.findAll()
    }

    @GET
    @Path("/persons/scores")
    fun getPersonalScores(
            @QueryParam("personId") personIds: List<Long>,
            @QueryParam("from") from: Long,
            @QueryParam("to") to: Long
    ): List<PersonalScoreDto> {
        // TODO SEC: check all personIds in caller groups

        return calculationService.getPersonalScoreNormalized(personIds, at.wuzeln.manager.Utils.longToLocalDateTime(from), at.wuzeln.manager.Utils.longToLocalDateTime(to))
    }

    @GET
    @Path("/persons/idleScore")
    fun getIdleScores(
            @QueryParam("personId") personIds: List<Long>,
            @QueryParam("from") from: Long,
            @QueryParam("to") to: Long
    ): List<IdleScoreDto> {
        // TODO SEC: check all personIds in caller groups

        return calculationService.calculateActiveScore(personIds, at.wuzeln.manager.Utils.longToLocalDateTime(from), at.wuzeln.manager.Utils.longToLocalDateTime(to))
    }
    // matches

    @GET
    @Path("/matches")
    fun findAllMatches(): List<MatchDto> {
        // TODO SEC: where caller is in groups

        return matchService.findAll()
    }

    @POST
    @Path("/matches")
    fun createMatch(match: MatchCreationDto): Long {
        // TODO SEC: check caller is in groups

        return matchService.createMatch(match)
    }

    @GET
    @Path("/matches/{matchId}")
    fun getMatch(@PathParam(value = "matchId") matchId: Long): MatchDetailedDto {
        // TODO SEC: check caller is in groups

        return matchService.getMatch(matchId)
    }

    @POST
    @Path("/matches/{matchId}/start")
    fun startMatch(@PathParam(value = "matchId") matchId: Long) {
        // TODO SEC: check caller is in match or creator

        matchService.startMatch(matchId)
    }

    @POST
    @Path("/matches/{matchId}/persons/{personId}/goal")
    fun kickGoal(
            @PathParam(value = "matchId") matchId: Long,
            @PathParam(value = "personId") personId: Long,
            goalDto: GoalCreationDto): MatchDetailedDto {
        // TODO SEC: check caller is in match or creator

        matchService.kickGoal(matchId, personId, goalDto.own)
        return matchService.getMatch(matchId)
    }

    @DELETE
    @Path("/matches/{matchId}/goals/last")
    fun revertLastGoal(
            @PathParam(value = "matchId") matchId: Long): MatchDetailedDto {
        // TODO SEC: check caller is in match or creator

        matchService.revertLastGoal(matchId)
        return matchService.getMatch(matchId)
    }

    @GET
    @Path("/matches/{matchId}/statistics")
    fun getMatchStats(@PathParam(value = "matchId") matchId: Long): MatchBasicStatDto {
        // TODO SEC: check caller is in groups

        return statsService.getBasicMatchStats(matchId)
    }

    // registrations

    @GET
    @Path("/registrations")
    fun findAllRegistrations(): List<RegistrationDto> {
        // TODO SEC: where caller is in groups

        return registrationService.findAllOpened()
    }

    @GET
    @Path("/registrations/{registrationId}")
    fun getRegistration(
            @PathParam(value = "registrationId") registrationId: Long
    ): RegistrationDto {
        // TODO SEC: check is caller in the group

        return registrationService.getRegistration(registrationId)
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/registrations")
    fun createRegistration(name: String): Long {
        // TODO SEC: create for group (add to input); check is caller in the group

        return registrationService.createRegistration(name)
    }

    @PUT
    @Path("/registrations/{registrationId}")
    fun updateRegistration(
            @PathParam(value = "registrationId") registrationId: Long,
            reg: RegistrationUpdateDto
    ): RegistrationDto {
        // TODO SEC: check is caller in the group

        return registrationService.updateRegistration(registrationId, reg)
    }

    @GET
    @Path("/registrations/{registrationId}/proposals/{method}")
    fun getProposal(@PathParam(value = "registrationId") registrationId: Long,
                    @PathParam(value = "method") method: MatchCreationMethod): MatchCreationDto {
        // TODO SEC: check is caller in the group

        return registrationService.generateMatchProposal(registrationId, method)
    }
}
