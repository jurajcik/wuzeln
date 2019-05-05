package at.wuzeln.manager.model.stat

import at.wuzeln.manager.model.Match
import at.wuzeln.manager.model.Team
import javax.persistence.*

@Entity
@Table(name = "STATS_MATCH")
class MatchStats(

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        @OneToOne
        val match: Match,

        @OneToOne
        val winner: Team,

        @OneToOne
        val looser: Team,

        val winnerScore: Int,
        val winnerGoals: Int,
        val winnerGoalsOwn: Int,

        val looserScore: Int,
        val looserGoals: Int,
        val looserGoalsOwn: Int
) {
    override fun toString(): String {
        return "MatchStats(id=$id, match=$match, winner=${winner.id}, looser=${looser.id}, winnerScore=$winnerScore, winnerGoals=$winnerGoals, winnerGoalsOwn=$winnerGoalsOwn, looserScore=$looserScore, looserGoals=$looserGoals, looserGoalsOwn=$looserGoalsOwn)"
    }
}
