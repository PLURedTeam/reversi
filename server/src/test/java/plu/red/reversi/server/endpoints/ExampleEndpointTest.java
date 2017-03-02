package plu.red.reversi.server.endpoints;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Created by daniel on 3/1/17.
 * Glory to the Red Team.
 */
public class ExampleEndpointTest extends EndpointTest {
    @Before
    public void prepareTarget() {
        target = c.target("http://localhost:8080/reversi/example");
    }

    @Test
    public void testPlainText() {
        String res = target.request(MediaType.TEXT_PLAIN_TYPE).get(String.class);

        Assert.assertEquals("This is an example response!", res);
    }

    @Test
    public void testThingJson() {
        String res = target.request(MediaType.APPLICATION_JSON).get(String.class);

        Assert.assertEquals("{\"foo\":\"Fun\",\"bar\":2,\"baz\":true}", res);
    }

    @Test
    public void testThingJsonPost() {
        String thingJson = "{\"foo\":\"wrar\",\"bar\":123}";

        String res = target.request(MediaType.TEXT_PLAIN_TYPE).post(Entity.json(thingJson)).readEntity(String.class);

        Assert.assertEquals(res, "Got foo wrar and bar 123");
    }
}
