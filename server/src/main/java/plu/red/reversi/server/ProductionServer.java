package plu.red.reversi.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import plu.red.reversi.server.Managers.SessionManager;

import javax.ws.rs.ApplicationPath;
import java.net.URI;

/**
 * The main entry for the server. Creates and starts the server as well as the necessary
 * utilities for the server to handle http traffic
 *
 * Glory to the Red Team.
 */
@ApplicationPath("reversi")
public class ProductionServer extends ResourceConfig {
    public ProductionServer() {
        packages("plu.red.reversi.server.endpoints");
    }//constructor
}//class