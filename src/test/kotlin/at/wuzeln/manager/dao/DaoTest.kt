package at.wuzeln.manager.dao

import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import at.wuzeln.manager.config.AppConfig
import at.wuzeln.manager.model.Person
import at.wuzeln.manager.model.UserAccount
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional


@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfig::class])
@DirtiesContext
open class DaoTest {

    @Autowired
    lateinit var personRepository: PersonRepository
    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @BeforeEach
    fun onSetUp() {
        val auth = UsernamePasswordAuthenticationToken("someUsername", null)
        SecurityContextHolder.getContext().authentication = auth
    }

    @Test
    @Transactional
    open fun contextLoads() {
        val name = "test person"
        val person = Person(0, UserAccount(0,   name), name)

        userAccountRepository.save(person.userAccount);
        personRepository.save(person)

        val result = personRepository.findAll()
        assertk.assert(result).hasSize(1)
        assertk.assert(result[0].userAccount.username).isEqualTo(name)

    }

}