package plu.red.reversi.client.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Andrew on 3/8/2017.
 */
public class DBUtilitiesTest {

    ConnectDB util; //ConnectDB Object

    @Before
    public void setUp() throws Exception {
        util = new ConnectDB();
    }//setUp

    @After
    public void tearDown() throws Exception {
        if(util.getConn() != null)
            util.closeDB();
    }//tearDown

    @Test
    public void openDB() throws Exception {
        String conn = util.openDB();
        Assert.assertEquals("Successfully connected to database", conn);
        Assert.assertNotEquals("Error connecting to database", conn);
    }//openDB

    @Test
    public void closeDB() throws Exception {
        util.openDB(); //Open the DBUtilities to test closure
        String conn = util.closeDB();
        Assert.assertEquals("Successfully disconnected from database", conn);
        Assert.assertNotEquals("Failed to close util connection: ", conn);
    }//closeDB
}//ConnectDBTest