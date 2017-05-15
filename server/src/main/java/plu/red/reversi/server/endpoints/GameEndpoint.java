package plu.red.reversi.server.endpoints;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import plu.red.reversi.core.util.GamePair;
import plu.red.reversi.core.util.User;
import plu.red.reversi.server.Managers.GameManager;
import plu.red.reversi.server.Managers.UserManager;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andrew on 3/23/17.
 * Glory to the Red Team.
 *
 * A set of endpoints that will be accessed from the game URI
 * with a path parameter that includes the game id.
 */
@Singleton
@Path("game")
public class GameEndpoint {

    //The broadcasters for the games
    public static HashMap<Integer, SseBroadcaster> games = new HashMap<Integer, SseBroadcaster>();

    /**
     * Adds the move that is sent from a client into the game with the
     * id from the path parameter
     * @param id the game id
     * @param move the command to be applied
     * @return true if applied to game, false otherwise
     */
    @Path("{id}")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void sendMove(@PathParam("id") int id, String move) {
        if(!GameManager.INSTANCE.gameExists(id))
            throw new WebApplicationException(404);

        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.mediaType(MediaType.TEXT_PLAIN_TYPE)
                .name("move")
                .data(String.class, move)
                .build();
        games.get(id).broadcast(event); //broadcast the move
    }//sendMove

    /**
     * Returns an eventOutput object so that the client can listen for
     *  moves that have been applied to the game
     * @param id the game id to listen to
     * @return the eventOutput item for the client
     */
    @Path("{id}")
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getMoves(@PathParam("id") int id) {
        if(!GameManager.INSTANCE.gameExists(id))
            throw new WebApplicationException(404);

        final EventOutput eventOutput = new EventOutput();
        games.get(id).add(eventOutput); //Add the listener to the game
        return eventOutput; //return the listener
    }//getMove

    /**
     * Creates a new network game on the server. This game will have status "WAITING ON PLAYERS"
     * until the game is started
     * @param user The user that wants to start the new network game
     * @return the gameID associated with the new network game
     */
    @Path("create/{num}/{name}/{type}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response createGame(@PathParam("num") int numPlayers, @PathParam("name") String name, @PathParam("type") GamePair.GameType type, User user) {
        if(!UserManager.INSTANCE.loggedIn(user.getUsername()))
            throw new WebApplicationException(403);
        int gameID = GameManager.INSTANCE.createGame(numPlayers, name,type);

        //Create a broadcaster
        SseBroadcaster broadcaster = new SseBroadcaster();
        games.put(gameID, broadcaster);

        user.setHost(true);
        GameManager.INSTANCE.addPlayer(gameID,user);
        return Response.ok(gameID).build();
    }//createGame

    /**
     * Adds the user to a game that is waiting on players
     * @param gameID the gameID of the game to add the user to
     * @param user The user to add to the network game
     * @return true if user added to game, false otherwise
     */
    @Path("join/{id}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response joinGame(@PathParam("id") int gameID, User user) {
        if(!UserManager.INSTANCE.loggedIn(user.getUsername()))
            throw new WebApplicationException(403);

        user.setHost(false);
        boolean joined = GameManager.INSTANCE.addPlayer(gameID, user);

        if(!GameManager.INSTANCE.gameExists(gameID))
            throw new WebApplicationException(404);

        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.mediaType(MediaType.APPLICATION_JSON_TYPE)
                .name("join")
                .data(User.class, user)
                .build();
        games.get(gameID).broadcast(event); //broadcast the move

        if(!joined) throw new WebApplicationException(410);

        return Response.ok(gameID).build();
    }//joinGame


    /**
     * Adds the user to a game that is waiting on players
     * @param gameID the gameID of the game to add the user to
     * @param user The user to add to the network game
     * @return true if user added to game, false otherwise
     */
    @Path("leave/{id}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response leaveGame(@PathParam("id") int gameID, User user) {
        if(!UserManager.INSTANCE.loggedIn(user.getUsername()))
            throw new WebApplicationException(403);

        boolean removed = GameManager.INSTANCE.removePlayer(gameID, user);

        if(!GameManager.INSTANCE.gameExists(gameID))
            throw new WebApplicationException(404);

        return Response.ok(gameID).build();
    }//joinGame




    /**
     * Adds the move that is sent from a client into the game with the
     * id from the path parameter
     * @param id the game id
     * @param game the game to start
     */
    @Path("start/{id}")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void startGame(@PathParam("id") int id, String game) {
        if(!GameManager.INSTANCE.gameExists(id))
            throw new WebApplicationException(404);

        boolean started = GameManager.INSTANCE.startGame(id); //Set the game status

        if(!started)
            throw new WebApplicationException(406);

        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.mediaType(MediaType.TEXT_PLAIN_TYPE)
                .name("start")
                .data(String.class, game)
                .build();
        games.get(id).broadcast(event); //broadcast the starting game
    }//startGame


    /**
     * Takes the final score for the player of a game in order to update
     * the users statistics in the database.
     * @param score The score of the player for the game
     */
    @Path("score/{score}")
    @POST
    public void gameScore(@PathParam("score") int score) {

    }//gameScore


    /**
     * Gets the games that are currently on the server and returns an arraylist
     * with the current game pairs (gameID, GamePair)
     */
    @Path("get-games")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGames() {
        ArrayList<GamePair> g = GameManager.INSTANCE.getGames();
        if(g.isEmpty()) throw new WebApplicationException(404);

        //Create a wrapper to send collection without breaking it apart
        GenericEntity<ArrayList<GamePair>> users = new GenericEntity<ArrayList<GamePair>>(g) {};

        return Response.ok(users).build();
    }//getGames

}//GameEndpoint
