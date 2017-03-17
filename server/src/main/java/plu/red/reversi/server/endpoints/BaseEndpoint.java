package plu.red.reversi.server.endpoints;

import com.sun.xml.internal.bind.v2.TODO;
import plu.red.reversi.core.util.User;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Andrew on 3/14/17.
 * Glory to the Red Team.
 *
 * A set of endpoints that will be accessed from the base URI
 * These endpoints are not user specific and are to be used for
 * basic server interactions (i.e. non game or chat functions)
 */
public class BaseEndpoint {

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

       // TODO: Connect with database to authenticate user

       //TODO: Set the password to false in the object for security reasons

        return null;
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

    /**
     * Login method to authenticate the users credentials
     * @param username The requested username of the user
     * @param password The password of the new user, transmitted in SHA256
     *                 format
     * @return true if user was created, false if username exists
     */
    @Path("create-user")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String createUser(String username, String password) {
        return "true";
    }//createUser

    /**
     * Deletes the user from the server database and removes all
     * saved games from the database. If user is logged in, will
     * logout user and terminate any games in progress.
     * @param username the users username
     * @param password the password of the user, transmitted in SHA256
     *                 format
     * @return true if user is deleted, false if incorrect credentials
     */
    @Path("delete-user")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteUser(String username, String password) {
        return "true";
    }//deleteUser

    /**
     * Gets the users currently online and their current status
     * (i.e. in lobby, in game)
     * @return A JSON object of users currently online
     */
    @Path("online-users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOnlineUsers() {
        return null;
    }//getOnlineUsers

    /**
     * Gets the current status of the leaderboard
     * @return A JSON Object of the current leaderboard
     */
    @Path("leaderboard")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLeaderboard() {
        return null;
    }//getLeaderboard

}//BaseEndpoint
