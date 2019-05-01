package at.wuzeln.manager

import org.h2.tools.Server
import java.sql.SQLException

object H2ConsoleUtil {


    internal var webServer: Server? = null

    /**
     * Make breakpoint block only the current thread (right-click on the breakpoint). <br></br>
     * Open in browser http://localhost:8082 . <br></br>
     * JDBC URL: jdbc:h2:mem:H2_SEA_PAA <br></br><br></br>
     *
     *
     * Call in @BeforeClass
     */
    fun start() {
        try {
            if (webServer == null) {
                webServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082", "-webDaemon")
                webServer!!.start()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    /**
     * Call in @AfterClass
     */
    fun stop() {
        if (webServer != null && webServer!!.isRunning(true)) {
            webServer!!.stop()
        }
    }

}