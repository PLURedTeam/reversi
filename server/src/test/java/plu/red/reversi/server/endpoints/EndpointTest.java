package plu.red.reversi.server.endpoints;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import plu.red.reversi.server.LocalServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by daniel on 3/1/17.
 *
 * Glory to the Red Team.
 */
public class EndpointTest {

    private static HttpServer server = null;
    protected static Client c = null;

    protected static WebTarget target;

    @BeforeClass
    public static void startServer() {
        server = LocalServer.startServer();
        c = ClientBuilder.newClient();
    }

    @AfterClass
    public static void stopServer() {
        c.close();
        server.shutdownNow();
    }
}
