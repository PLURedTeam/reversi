package plu.red.reversi.server.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by daniel on 3/1/17.
 *
 * Glory to the Red Team.
 */
@Path("example")
public class ExampleEndpoint {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is an example response!";
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThing() {
        return Response.ok().entity(new Thing("Fun", 2, true)).build();
    }
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String receiveJoke(Thing j) {
        return "Got foo " + j.foo + " and bar " + j.bar;
    }

    private static class Thing {
        public String foo;
        public int bar;
        public boolean baz;

        public Thing() {
            this("", 0, false);
        }

        public Thing(String foo, int bar, boolean baz) {
            this.foo = foo;
            this.bar = bar;
            this.baz = baz;
        }
    }
}
