package at.wuzeln.manager.model

import at.wuzeln.manager.model.enums.TeamColor
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
data class Team(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @Enumerated(EnumType.STRING)
        val color: TeamColor,

        @OneToMany(mappedBy = "team", cascade = [CascadeType.ALL])
        @OnDelete(action = OnDeleteAction.CASCADE)
        val players: MutableList<Player>
) {

    init {
        players.forEach { it.team = this }
    }

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var otherTeam: Team

    @OneToOne(cascade = [CascadeType.ALL])
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var match: Match

    var winner: Boolean = false
}