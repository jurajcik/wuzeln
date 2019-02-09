package at.wuzeln.manager.dao

import at.wuzeln.manager.model.EntityMetadata
import at.wuzeln.manager.model.HasMetadata
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

class MetadataInjector {

    @PrePersist
    fun persist(hasMetadata: HasMetadata){

        var auth = SecurityContextHolder.getContext().authentication

        val now = LocalDateTime.now()

        hasMetadata.metadata = EntityMetadata(now, auth.name)
        hasMetadata.metadata?.modifiedDate = now
        hasMetadata.metadata?.modifiedUser = auth.name
    }

    @PreUpdate
    fun update(hasMetadata: HasMetadata){

        var auth = SecurityContextHolder.getContext().authentication

        val now = LocalDateTime.now()

        if(hasMetadata.metadata == null){
            hasMetadata.metadata = EntityMetadata(null, null)
        }

        hasMetadata.metadata?.modifiedDate = now
        hasMetadata.metadata?.modifiedUser = auth.name
    }

}