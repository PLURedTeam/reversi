package plu.red.reversi.client.util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Created by Andrew on 3/15/2017.
 */
public class WebUtilities {

    private Client client = null;
    private String baseURI = "http://localhost:8080/reversi/"; //Just temp, will change with production server

    public WebUtilities() {
        //create the client
        client = ClientBuilder.newClient();
    }//webUtilities

    public void login() {
        WebTarget target = client.target("url" + "login");
    }//login


}//webUtilities
