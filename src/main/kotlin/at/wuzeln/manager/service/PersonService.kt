package at.wuzeln.manager.service

import at.wuzeln.manager.dao.PersonRepository
import at.wuzeln.manager.dao.UserAccountRepository
import at.wuzeln.manager.dto.PersonDto
import at.wuzeln.manager.model.Person
import at.wuzeln.manager.model.enums.SecurityRole
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PersonService(
        private val personRepository: PersonRepository,
        private val userAccountRepository: UserAccountRepository
) {


    @Transactional
    fun createPerson(username: String, nickname: String): Long {

        val userAccount = userAccountRepository.findByUsername(username)
                ?: throw WuzelnException("There is no user account for username: $username")

        val person = Person(0, userAccount, nickname)

        return personRepository.save(person).id
    }

    @Transactional
    fun findAll(): List<PersonDto> {
        val persons = personRepository.findAll()

        return persons
                .map {
                    PersonDto(
                            it.id,
                            it.nickname,
                            it.userAccount.hasRole(SecurityRole.ACTIVE_USER)
                    )
                }
                .toList()
    }
}
