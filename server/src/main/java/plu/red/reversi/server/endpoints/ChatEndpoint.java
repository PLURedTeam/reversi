package plu.red.reversi.server.endpoints;

import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import plu.red.reversi.core.util.ChatMessage;
import org.glassfish.jersey.media.sse.SseBroadcaster;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.inject.Singleton;

/**
 * Created by Andrew on 3/23/17.
 * Glory to the Red Team.
 *
 * A set of endpoints that will be accessed from the base URI
 * These endpoints are not user specific and are to be used for
 * basic server interactions (i.e. non game or chat functions)
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
    public EventOutput listenToBroadcast() {
        final EventOutput eventOutput = new EventOutput();
        this.broadcaster.add(eventOutput);
        return eventOutput;
    }//getMessages
}//BaseEndpoint
