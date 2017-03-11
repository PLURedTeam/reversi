package plu.red.reversi.client.database;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Andrew on 3/8/2017.
 */
public class ClientUtilitiesTest {

    ClientUtilities util = new ClientUtilities(); //ClientUtilities Object

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void openDB() throws Exception {
        String conn = util.openDB();

        Assert.assertEquals("Successfully connected to database", conn);
        Assert.assertNotEquals("Error connecting to database", conn);

        //CreateDB db = new CreateDB();
    }

    @Test
    public void closeDB() throws Exception {

    }

    @Test
    public void getConn() throws Exception {

    }

    @Test
    public void setConn() throws Exception {

    }

}