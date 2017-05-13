package plu.red.reversi.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import plu.red.reversi.server.Managers.SessionManager;

import java.net.URI;

/**
 * The main entry for the server. Creates and starts the server as well as the necessary
 * utilities for the server to handle http traffic
 *
 * Glory to the Red Team.
 */
public class LocalServer {

    public static final String BASE_URI = "http://localhost:8080/reversi";//The base URI for the server
    private static HttpServer server;//the server object

    /**
     * Starts the server
     * @return the HttpServer Object
     */
    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig(SseFeature.class).packages("plu.red.reversi.server.endpoints");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }//startServer

    /**
     * The main entry point for the server program
     * @param args commandline arguments (not used)
     */
    public static void main(String[] args) {

        //Start the session manager
        new Thread(SessionManager.INSTANCE).start();

        //Start the server
        server = startServer();
    }//main
}//class