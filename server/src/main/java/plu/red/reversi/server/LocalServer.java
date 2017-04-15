package plu.red.reversi.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import plu.red.reversi.core.db.DBUtilities;
import plu.red.reversi.server.Chat.ChatHandler;

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
    public static ChatHandler globalChat;

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
        SessionManager.INSTANCE = new SessionManager();
        new Thread(SessionManager.INSTANCE).start();

        //Create the user manager
        UserManager.INSTANCE = new UserManager();
        DBUtilities.INSTANCE = new DBUtilities();
        globalChat = new ChatHandler();

        //Start the server
        server = startServer();
    }//main
}//class