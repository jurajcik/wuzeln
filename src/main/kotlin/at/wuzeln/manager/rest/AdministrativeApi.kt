package at.wuzeln.manager.rest

import at.wuzeln.manager.dto.ConfigurationDto
import at.wuzeln.manager.dto.UserAccountCreationDto
import at.wuzeln.manager.dto.UserAccountDto
import at.wuzeln.manager.dto.UserAccountUpdateAdminDto
import at.wuzeln.manager.model.enums.SecurityRole
import at.wuzeln.manager.service.PersonService
import at.wuzeln.manager.service.UserAccountService
import at.wuzeln.manager.service.WuzelnException
import mu.KotlinLogging
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Component
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
class AdministrativeApi(
        private val userAccountService: UserAccountService,
        private val configuration: ConfigurationDto,
        private val personService: PersonService
) {

    private val log = KotlinLogging.logger {}

    @POST
    @Path("/administration/users")
    fun createUserAccount(userAccount: UserAccountCreationDto): Long {

        var auth = SecurityContextHolder.getContext().authentication

        if (hasRole(SecurityRole.UNKNOWN)) {
            val userAccountId = userAccountService.register(userAccount.username, auth.principal as String)
            personService.createPerson(userAccount.username, userAccount.username)
            return userAccountId
        }

        throw WuzelnException("This user is already registered under username=${auth.name}");
    }


    @GET
    @Path("/administration/users/current")
    fun getUserAccount(): UserAccountDto {

        var auth = SecurityContextHolder.getContext().authentication

        if (hasRole(SecurityRole.UNKNOWN)) {
            return UserAccountDto(null, null, false, false, null)
        } else {
            return userAccountService.getUserAccount(auth.name)
        }
    }


    @GET
    @Path("/administration/users")
    fun getUserAccounts(): List<UserAccountDto> {
        return userAccountService.getUserAccounts()
    }


    @PUT
    @Path("/administration/users/{username}")
    fun updateUserAccount(
            @PathParam("username") username: String,
            userAccount: UserAccountUpdateAdminDto
    ) {
        userAccountService.updateUserAccount(username, userAccount)
    }


    fun hasRole(role: SecurityRole): Boolean {
        return SecurityContextHolder.getContext().authentication.authorities.find { it.authority.equals(role.nameWithPrefix()) } != null
    }
}
