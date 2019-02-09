package at.wuzeln.manager.model

import at.wuzeln.manager.model.enums.SecurityRole
import javax.persistence.*


@Entity
data class Role(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @Column(unique = true)
        @Enumerated(EnumType.STRING)
        val name: SecurityRole
)