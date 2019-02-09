package at.wuzeln.manager.model

import java.time.LocalDateTime
import javax.persistence.Embeddable


@Embeddable
class EntityMetadata(
        var createdDate: LocalDateTime?,
        var createdUser: String?
) {
    var modifiedDate: LocalDateTime? = null
    var modifiedUser: String? = null
}