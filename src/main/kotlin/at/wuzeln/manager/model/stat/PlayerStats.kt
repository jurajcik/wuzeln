package at.wuzeln.manager.model.stat

import at.wuzeln.manager.model.Player
import javax.persistence.*

@Entity
@Table(name = "STATS_PLAYER")
class PlayerStats(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @OneToOne
        val player: Player,

        val goalsSum: Int,

        val goalsOwnSum: Int,

        val timeInGoalMillisSum: Int,

        val scoreOffensive: Double,

        val scoreDefensive: Double,

        val scoreOffensiveNormalizedRange: Double,

        val scoreDefensiveNormalizedRange: Double
) {

    override fun toString(): String {
        return "PlayerStats(id=$id, player=$player, goalsSum=$goalsSum, goalsOwnSum=$goalsOwnSum, timeInGoalMillisSum=$timeInGoalMillisSum, scoreOffensive=$scoreOffensive, scoreDefensive=$scoreDefensive, scoreOffensiveNormalizedRange=$scoreOffensiveNormalizedRange, scoreDefensiveNormalizedRange=$scoreDefensiveNormalizedRange)"
    }
}