package at.wuzeln.manager.model

import at.wuzeln.manager.dao.MetadataInjector
import at.wuzeln.manager.model.enums.Position
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@EntityListeners(MetadataInjector::class)
data class Goal(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        val date: LocalDateTime,

        val own: Boolean,

        @Enumerated(EnumType.STRING)
        val position: Position,

        @ManyToOne
        val player: Player,

        @ManyToOne
        val goalie: Player

) : HasMetadata {

        @Embedded
        override var metadata: EntityMetadata? = null
}
