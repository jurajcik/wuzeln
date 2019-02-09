package at.wuzeln.manager.rest

import at.wuzeln.manager.service.WuzelnException
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils
import java.lang.reflect.UndeclaredThrowableException
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider


@Provider
class CustomExceptionMapper : ExceptionMapper<Throwable> {

    private val log = KotlinLogging.logger {}

    override fun toResponse(t: Throwable?): Response {

        return if (t is WebApplicationException) {
            log.warn("", t)
            Response.status(t.response.status)
                    .entity(t.response.statusInfo.reasonPhrase)
                    .build()
        } else if (t is WuzelnException ) {
            Response.status(Response.Status.BAD_REQUEST)
                    .entity(t.message)
                    .build()

        } else if (t is UndeclaredThrowableException && ExceptionUtils.getRootCause(t) is WuzelnException) {
            Response.status(Response.Status.BAD_REQUEST)
                    .entity(ExceptionUtils.getRootCause(t).message)
                    .build()
        } else {
            log.error("", t)
            Response.serverError()
                    .entity(t?.message)
                    .build()
        }
    }
}


