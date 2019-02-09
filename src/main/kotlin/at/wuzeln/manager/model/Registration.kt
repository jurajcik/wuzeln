package at.wuzeln.manager.model

import at.wuzeln.manager.dao.MetadataInjector
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@EntityListeners(MetadataInjector::class)
data class Registration(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        val id: Long = 0,

        var name: String

): HasMetadata {

    @Embedded
    override var metadata: EntityMetadata? = null

    @ManyToMany
    val persons: MutableList<Person> = ArrayList()

    @OneToOne
    var match: Match? = null

    fun isOpened(): Boolean {
        return match == null
    }

    fun isClosed(): Boolean {
        return !isOpened()
    }

    fun getCreatedDate():LocalDateTime{
        return metadata?.createdDate as LocalDateTime
    }
}