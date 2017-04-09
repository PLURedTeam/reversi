package plu.red.reversi.server.endpoints;

import plu.red.reversi.core.command.Command;
import plu.red.reversi.core.util.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Andrew on 3/23/17.
 * Glory to the Red Team.
 *
 * A set of endpoints that will be accessed from the game URI
 * with a path parameter that includes the game id.
 */
@Path("game")
public class GameEndpoint {

    /**
     * Adds the move that is sent from a client into the game with the
     * id from the path parameter
     * @param id the game id
     * @param c the command to be applied
     * @return true if applied to game, false otherwise
     */
    @Path("{id}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public boolean sendMove(@PathParam("id") int id, Command c) {

        return true;
    }//sendMove

    /**
     * Returns a move that has been played by another player in the game
     * @param id the game id to get the move from
     * @return the Command that has been played by a network player
     */
    @Path("{id}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Command getMove(@PathParam("id") int id) {

        return null;
    }//getMove
}//BaseEndpoint
