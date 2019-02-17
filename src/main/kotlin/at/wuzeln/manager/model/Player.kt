package at.wuzeln.manager.model

import at.wuzeln.manager.model.enums.Position
import at.wuzeln.manager.model.stat.PlayerStats
import javax.persistence.*

@Entity
data class Player(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @Enumerated(EnumType.STRING)
        val startingPosition: Position,

        @ManyToOne
        val person: Person
) {

    init {
        this.person.played.add(this)
    }

    private var milisecondsInGoal: Long = 0

    @ManyToOne
    lateinit var team: Team

    @ManyToOne
    lateinit var playerLeft: Player

    @ManyToOne
    lateinit var playerRight: Player

    @OneToOne(mappedBy = "player", orphanRemoval = true)
    lateinit var stats: PlayerStats

    @OneToMany(mappedBy = "player", cascade = [CascadeType.ALL], orphanRemoval = true)
    val goals: MutableList<Goal> = ArrayList()

    fun getMilisicondsInGoal(): Long {
        return this.milisecondsInGoal
    }

    fun addMilisecondsInGoal(miliseconds: Long) {
        this.milisecondsInGoal += miliseconds
    }

    fun subtractMilisecondsInGoal(miliseconds: Long) {
        this.milisecondsInGoal -= miliseconds
    }

}