//package plu.red.reversi.client.database;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
///**
// * Created by Andrew on 3/12/2017.
// */
//public class ClientUtilitiesTest {
//    ClientUtilities util; //ConnectDB Object
//
//    @Before
//    public void setUp() throws Exception {
//        util = new ClientUtilities();
//    }//setUp
//
//    @After
//    public void tearDown() throws Exception {
//
//    }//tearDown
//
//    @Test
//    public void createUser() throws Exception {
//        boolean created = util.createUser("andy", "passwordTest");
//        boolean created1 = util.createUser("andy", "passwordTest");
//
//        Assert.assertTrue(created);
//        Assert.assertFalse(created1);
//
//    }//createUser
//
//    @Test
//    public void getUsers() throws Exception {
//        String[] users = util.getUsers();
//
//        for(int i = 0; i < users.length; i++)
//            System.out.println(users[i]);
//    }
//
//    @Test
//    public void login() throws Exception {
//        boolean login = util.login("andy", "passwordTest");
//        boolean login1 = util.login("andy", "passwordTest1");
//
//        Assert.assertTrue(login);
//        Assert.assertFalse(login1);
//    }
//
//    @Test
//    public void deleteUser() throws Exception {
//        boolean deleted = util.deleteUser("andy", "passwordTest");
//        boolean deleted1 = util.deleteUser("andy", "passwordTest");
//
//        Assert.assertTrue(deleted);
//        Assert.assertFalse(deleted1);
//    }
//
//    @Test
//    public void createGame() throws Exception {
//
//    }
//
//    @Test
//    public void loadGame() throws Exception {
//
//    }
//
//    @Test
//    public void getGames() throws Exception {
//
//    }
//
//    @Test
//    public void saveGameSettings() throws Exception {
//
//    }
//
//    @Test
//    public void loadGameSettings() throws Exception {
//
//    }
//
//}