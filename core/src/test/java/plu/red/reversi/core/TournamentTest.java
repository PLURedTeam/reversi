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
    @Before
    public void setUp() throws Exception {
        ArrayList<User> tournamentUsers = new ArrayList<User>(); //instantiating a contender arrayList for the tournament
        tournamentUsers.add(new User("u1", "u1"));
        tournamentUsers.add(new User("u2", "u2"));
        Tournament tournament1 = new Tournament(tournamentUsers);
    }

    @Test
    public void currentMatch() throws Exception {

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