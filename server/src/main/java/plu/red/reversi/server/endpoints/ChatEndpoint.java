package plu.red.reversi.server.endpoints;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.media.sse.SseBroadcaster;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.inject.Singleton;

/**
 * Created by Andrew on 3/23/17.
 *
 * A set of endpoints that will be accessed from the chat URI
 * These endpoints are to be used for chat among network users.
 *
 * Chat uses Jersey Server Sent Events. These events are broadcast
 * to all clients that currently have the eventOutput object. A client
 * can get the eventOutput object (which will be the eventInput on the
 * client side) by a GET request to the chat URI. To send a message, the
 * client will send a POST request to the chat URI with the message that
 * is to be broadcast.
 *
 * In order for the broadcaster to work properly, the class must be a
 * singleton. This is annotated below with the @Singleton flag. Without
 * this class as a singleton, each client that connects, will be given an
 * eventOutput object that belongs to a new broadcaster for that instance.
 */
@Singleton
@Path("/chat")
public class ChatEndpoint {

    //The broadcaster for the SSE
    private SseBroadcaster broadcaster = new SseBroadcaster();

    /**
     * Posts a message to the ChatHandler that was received from the client
     * @param message the chatmessage to broadcast to clients
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void postMessage(String message) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.mediaType(MediaType.TEXT_PLAIN_TYPE)
                .name("message")
                .data(String.class, message)
                .build();
        broadcaster.broadcast(event);
    }//postMessage

    /**
     * returns an eventOutput object that the client can use to listen for broadcasted messages
     * @return eventOutput item for the client
     */
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput registerListener() {
        final EventOutput eventOutput = new EventOutput();
        this.broadcaster.add(eventOutput);
        return eventOutput;
    }//getMessages
}//BaseEndpoint
