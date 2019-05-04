package at.wuzeln.manager.model

import at.wuzeln.manager.dao.MetadataInjector
import at.wuzeln.manager.model.enums.SecurityRole
import javax.persistence.*

// in PostgreSQL 'user' is a pseudo-function keyword
@Entity
@EntityListeners(MetadataInjector::class)
data class UserAccount(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @Column(unique = true)
        val username: String
): HasMetadata {

    @Column(unique = true)
    var googleAccountId: String? = null

    @Basic(fetch = FetchType.LAZY)
    @Lob
    lateinit var image: ByteArray

    @OneToMany(mappedBy = "userAccount")
    var persons: MutableList<Person> = ArrayList()

    @ManyToMany
    var roles: MutableSet<Role> = HashSet()

    @Embedded
    override var metadata: EntityMetadata? = null


    fun hasRole(scurityRole: SecurityRole): Boolean {
        return roles.find { it.name == scurityRole } != null
    }
}
