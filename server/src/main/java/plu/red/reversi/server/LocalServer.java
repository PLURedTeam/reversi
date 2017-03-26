package plu.red.reversi.server;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import plu.red.reversi.server.Chat.ChatHandler;

import java.net.URI;

/**
 * Created by daniel on 3/1/17.
 *
 * Glory to the Red Team.
 */
public class LocalServer {
    public static final String BASE_URI = "http://localhost:8080/reversi";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("plu.red.reversi.server.endpoints");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    private static HttpServer server;
    public static void main(String[] args) {

        SessionManager.INSTANCE = new SessionManager();
        new Thread(SessionManager.INSTANCE).start();

        server = startServer();
    }
}