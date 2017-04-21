package plu.red.reversi.core;

import org.junit.Before;
import org.junit.Test;
import plu.red.reversi.core.game.Tournament;
import plu.red.reversi.core.util.User;

import java.util.ArrayList;

/**
 * Created by JChase on 4/9/17.
 */
public class TournamentTest {
    private ArrayList<User> tournamentUsers;
    private Tournament tournament1;
    User u1, u2, u3, u4;


    @Before
    public void setUp() throws Exception {
        tournamentUsers = new ArrayList<User>(); //instantiating a contender arrayList for the tournament
        u1 = new User("u1", "u1");
        u2 = new User("u2", "u2");
        u3 = new User("u3", "u3");
        u4 = new User("u4", "u4");
        tournamentUsers.add(u1);
        tournamentUsers.add(u2);
        tournamentUsers.add(u3);
        tournamentUsers.add(u4);
        tournament1 = new Tournament(tournamentUsers);
    }

    @Test
    public void currentMatch() throws Exception {

        //Tournament.Match m = (new Tournament.Match(new Tournament.Pair(u1, u2), 0));
        Tournament.Match n = tournament1.currentMatch(1);
        // assertEquals(m, n);
    }

    @Test
    public void currentOpponents() throws Exception {

    }

    @Test
    public void matchNumber() throws Exception {

    }

    @Test
    public void nextMatch() throws Exception {

    }

    @Test
    public void setWinner() throws Exception {

    }

    @Test
    public void getWinner() throws Exception {

    }

}