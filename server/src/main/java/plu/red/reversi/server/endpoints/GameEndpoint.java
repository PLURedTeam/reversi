package plu.red.reversi.server.endpoints;

import plu.red.reversi.core.util.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Andrew on 3/23/17.
 * Glory to the Red Team.
 *
 * A set of endpoints that will be accessed from the base URI
 * These endpoints are not user specific and are to be used for
 * basic server interactions (i.e. non game or chat functions)
 */
@Path("game")
public class GameEndpoint {

    /**
     * Login method to authenticate the users credentials
     * @param user A user object that holds the username and password
     * @return User object that will include true if correct credentials and also the
     *         session id, will be false if incorrect credentials and null for
     *         the session id.
     */
    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public User login(User user) {

        System.out.print("I AM IN LOGIN");

        if(user.getUsername().equals("MajorSlime") && user.getPassword().equals("ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae")) {
            user.setSessionID(1);
            user.setPassword("");
        } else {
            throw new WebApplicationException(403);
        }//else

        return user;
    }//login

    /**
     * Logout method to logout the user from the server
     * @param user A user object that holds the username, and sessionID
     * @return true if users is logged out, false otherwise
     */
    @Path("logout")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String logout(User user) {
        return "true";
    }//logout

}//BaseEndpoint
