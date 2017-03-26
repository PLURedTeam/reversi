package plu.red.reversi.client.network;

import plu.red.reversi.core.util.User;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

/**
 * Created by Andrew on 3/15/2017.
 */
public class WebUtilities {

    public static WebUtilities INSTANCE = new WebUtilities();

    private Client client;
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server
    private int sessionID;
    private User user;

    public boolean loggedIn = false;

    /**
     * Constructor for WebUtilities
     * Creates a new Client Object
     */
    public WebUtilities() {
        //create the client
        client = ClientBuilder.newClient();
    }//webUtilities

    /**
     * Calls the server to authenticate the user credentials to login
     * to the server
     * @param username The players username
     * @param password The players password, should be passed in SHA256 format
     * @return true if valid credentials, false otherwise
     */
    public boolean login(String username, String password) {
        //Create User
        user = new User();
        user.setUsername(username);
        user.setPassword(password);

        //Create target and call server
        WebTarget target = client.target("http://localhost:8080/reversi/login");
        Response response = target.request().post(Entity.json(user));

        //If invalid credentials, return false
        if(response.getStatus() == 403) return false;
        user = response.readEntity(User.class);
        sessionID = user.getSessionID();
        loggedIn = true;

      //  PollingMachine machine = new PollingMachine(this, client, user);

        return true;
    }//login

    /**
     * Logs out the user from the server. Will probably only be used on
     * application exit or if we support connection to multiple servers
     */
    public boolean logout() {
        WebTarget target = client.target("url" + "logout");
        Response response = target.request().post(Entity.json(user));

        if(response.getStatus() != 200) return false;
        loggedIn = false;

        return true;
    }//logout

}//webUtilities
