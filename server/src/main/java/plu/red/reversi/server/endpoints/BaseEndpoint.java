package plu.red.reversi.server.endpoints;

import plu.red.reversi.server.Chat.ChatHandler;
import plu.red.reversi.server.LocalServer;
import plu.red.reversi.server.db.DBUtilities;
import plu.red.reversi.core.util.User;
import plu.red.reversi.server.SessionManager;
import plu.red.reversi.server.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by Andrew on 3/14/17.
 * Glory to the Red Team.
 *
 * A set of endpoints that will be accessed from the base URI
 * These endpoints are not user specific and are to be used for
 * basic server interactions (i.e. non game or chat functions)
 */
@Path("/")
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
    public Response login(User user) {
        if(user == null) throw new WebApplicationException(403);

        try {
            if(DBUtilities.INSTANCE.authenticateUser(user.getUsername(), user.getPassword())) {
                user.setPassword("");
            } else {
                throw new WebApplicationException(403);
            }//else
        } catch (NoSuchAlgorithmException e) {
            throw new WebApplicationException(500);
        }//catch

        user.setStatus("In Lobby");
        UserManager.INSTANCE.addUser(user);
        user.setSessionID(SessionManager.INSTANCE.addSession());
        LocalServer.globalChat.addUser(user.getUsername());

        return Response.ok().entity(user).build();
    }//login

    /**
     * Logout method to logout the user from the server
     * @param user A user object that holds the username, and sessionID
     * @return true if users is logged out, false otherwise
     */
    @Path("logout")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public boolean logout(User user) {

        if(user == null)
            throw new WebApplicationException(403);

        UserManager.INSTANCE.removeUser(user);
        SessionManager.INSTANCE.removeSession(user.getSessionID());
        LocalServer.globalChat.removeUser(user.getUsername());
        return true;
    }//logout

    /**
     * Create user method to add a new user to the database
     * @param user the user to be added to the database
     * @return true if user was created, false if username exists
     */
    @Path("create-user")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response createUser(User user) {

        if(user == null)
            throw new WebApplicationException(400);

        try {
            if(DBUtilities.INSTANCE.createUser(user.getUsername(),user.getPassword()))
                return Response.ok().build();
            else
                throw new WebApplicationException(406);
        } catch(NoSuchAlgorithmException e) {
            throw new WebApplicationException(500);
        }//catch
    }//createUser

    /**
     * Deletes the user from the server database and removes all
     * saved games from the database. If user is logged in, will
     * logout user and terminate any games in progress.
     * @param user the user to be deleted from the database
     * @return true if user is deleted, false if incorrect credentials
     */
    @Path("delete-user")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteUser(User user) {

        if(user == null)
            throw new WebApplicationException(400);

        try {
            if(DBUtilities.INSTANCE.deleteUser(user.getUsername(),user.getPassword()))
                return Response.ok().build();
            else
                throw new WebApplicationException(406);
        } catch(NoSuchAlgorithmException e) {
            throw new WebApplicationException(500);
        }//catch
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
        ArrayList<User> u = UserManager.INSTANCE.onlineUsers();
        if(u.isEmpty()) throw new WebApplicationException(404);

        //Create a wrapper to send collection without breaking it apart
        GenericEntity<ArrayList<User>> users = new GenericEntity<ArrayList<User>>(u) {};

        return Response.ok(users).build();
    }//getOnlineUsers

    /**
     * Gets the current status of the leaderboard
     * @return A JSON Object of the current leaderboard
     * TODO: Implement this once the ranking system is in place
     */
    @Path("leaderboard")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLeaderboard() {
        return null;
    }//getLeaderboard

    /**
     * Keeps the current session Alive on the server
     * @param sessionID
     */
    @Path("keep-session-alive/{id}")
    public Response keepSessionAlive(@PathParam("id") int sessionID) {
        SessionManager.INSTANCE.keepSessionAlive(sessionID);
        return Response.ok().build();
    }//keepSessionAlive
}//BaseEndpoint
