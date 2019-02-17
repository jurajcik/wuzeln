package at.wuzeln.manager.model.stat

import at.wuzeln.manager.model.Player
import at.wuzeln.manager.model.enums.Position
import javax.persistence.*

@Entity(name = "STATS_PLAYER")
class PlayerStats(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @OneToOne
        val player: Player,

        val goalsSum: Int,

        val goalsOwnSum: Int,

        val goalsAllAvg: Double,

        val timeInGoalMillisSum: Int,

        val timeInGoalMillisAvg: Int,

        val scoreOffensive: Double,

        val scoreDefensive: Double
) {

    override fun toString(): String {
        return "PlayerStats(id=$id, player=$player, goalsSum=$goalsSum, goalsOwnSum=$goalsOwnSum, goalsAllAvg=$goalsAllAvg, timeInGoalMillisSum=$timeInGoalMillisSum, timeInGoalMillisAvg=$timeInGoalMillisAvg, scoreOffensive=$scoreOffensive, scoreDefensive=$scoreDefensive)"
    }
}