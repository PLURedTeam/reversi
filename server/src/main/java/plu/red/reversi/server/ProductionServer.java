package plu.red.reversi.server;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

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