package at.wuzeln.manager.model

import at.wuzeln.manager.dao.MetadataInjector
import at.wuzeln.manager.model.enums.MatchCreationMethod
import at.wuzeln.manager.model.enums.TeamColor
import at.wuzeln.manager.model.stat.MatchStats
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.*

@Entity
@EntityListeners(MetadataInjector::class)
data class Match(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        val name: String,

        @OneToOne(cascade = [CascadeType.ALL])
        @OnDelete(action = OnDeleteAction.CASCADE)
        val teamBlue: Team,

        @OneToOne(cascade = [CascadeType.ALL])
        @OnDelete(action = OnDeleteAction.CASCADE)
        val teamRed: Team,

        @ManyToMany(mappedBy = "idleMatches")
        @OnDelete(action = OnDeleteAction.CASCADE)
        val idlePersons: List<Person>,

        @Enumerated(EnumType.STRING)
        val method: MatchCreationMethod
) : HasMetadata{

    init {
        this.teamBlue.match = this
        this.teamRed.match = this
        this.idlePersons.forEach { it.idleMatches.add(this) }
    }

    var startDate: LocalDateTime? = null
    var endDate: LocalDateTime? = null

    @Embedded
    override var metadata: EntityMetadata? = null

    @OneToOne(mappedBy = "match", cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var stats: MatchStats

    fun hasFinished(): Boolean {
        return endDate != null
    }

    fun hasStarted(): Boolean {
        return startDate != null
    }

    fun getWinner(): TeamColor? {
        return if (this.teamBlue.winner) {
            TeamColor.BLUE
        } else if (this.teamRed.winner) {
            TeamColor.RED
        } else {
            null
        }
    }

    fun getTeam(teamColor: TeamColor): Team {
        return when (teamColor) {
            TeamColor.BLUE -> this.teamBlue
            TeamColor.RED -> this.teamRed
        }
    }

    fun duration(): Long? {
        return if (this.startDate == null) {
            null
        } else if (this.endDate == null) {
            this.startDate?.until(LocalDateTime.now(), ChronoUnit.MILLIS)
        } else {
            this.startDate?.until(this.endDate, ChronoUnit.MILLIS)
        }
    }

    fun getCreatedDate():LocalDateTime{
        return metadata?.createdDate as LocalDateTime
    }

}
