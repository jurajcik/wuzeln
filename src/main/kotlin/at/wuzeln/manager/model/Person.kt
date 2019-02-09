package at.wuzeln.manager.model

import javax.persistence.*

@Entity
data class Person(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @ManyToOne
        var userAccount: UserAccount,

        val nickname: String
) {

    @OneToMany(mappedBy = "person")
    var played: MutableList<Player> = ArrayList()

    @ManyToMany
    var idleMatches: MutableList<Match> = ArrayList()

}