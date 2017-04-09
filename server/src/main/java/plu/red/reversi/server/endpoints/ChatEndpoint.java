package plu.red.reversi.server.endpoints;

import org.codehaus.jettison.json.JSONObject;
import plu.red.reversi.core.util.ChatMessage;
import plu.red.reversi.core.util.User;
import plu.red.reversi.server.Chat.ChatHandler;
import plu.red.reversi.server.LocalServer;
import plu.red.reversi.server.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * Created by Andrew on 3/23/17.
 * Glory to the Red Team.
 *
 * A set of endpoints that will be accessed from the base URI
 * These endpoints are not user specific and are to be used for
 * basic server interactions (i.e. non game or chat functions)
 */
@Path("chat")
public class ChatEndpoint {

    ChatHandler globalChat = new ChatHandler();

    /**
     * Posts a message to the ChatHandler that was received from the client
     * @param message
     */
    @Path("global")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void postMessage(ChatMessage message) {
        globalChat.postMessage(message);
    }//postMessage


    @Path("global/{user}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getMessages(@PathParam("user") String username) {

        if(UserManager.INSTANCE.loggedIn(username) == false) throw new WebApplicationException(404);
        if(globalChat.getMessages(username) == null) throw new WebApplicationException(404);
        return null;
    }//getLeaderboard

}//BaseEndpoint
