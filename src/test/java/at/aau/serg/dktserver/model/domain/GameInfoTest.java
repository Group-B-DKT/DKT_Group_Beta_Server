package at.aau.serg.dktserver.model.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameInfoTest {
    @Test
    void testEqualsTrue(){
        GameInfo gameInfo = new GameInfo(1, "Test", new ArrayList<>());
        GameInfo gameInfo2 = new GameInfo(1, "Test", new ArrayList<>());
        assertTrue(gameInfo.equals(gameInfo2));
    }

    @Test
    void testEqualsFalse(){
        GameInfo gameInfo = new GameInfo(1, "Test", new ArrayList<>());
        GameInfo gameInfo2 = new GameInfo(1, "Test2", new ArrayList<>());
        assertFalse(gameInfo.equals(gameInfo2));
    }

    @Test
    void testHashTrue(){
        GameInfo gameInfo = new GameInfo(1, "Test", new ArrayList<>());
        GameInfo gameInfo2 = new GameInfo(1, "Test", new ArrayList<>());
        assertTrue(gameInfo.hashCode() == gameInfo2.hashCode());
    }

    @Test
    void testHashFalse(){
        GameInfo gameInfo = new GameInfo(1, "Test", new ArrayList<>());
        GameInfo gameInfo2 = new GameInfo(1, "Test2", new ArrayList<>());
        assertFalse(gameInfo.hashCode() == gameInfo2.hashCode());
    }

    @Test
    void testSetId(){
        GameInfo gameInfo = new GameInfo(1, "Test", null);
        gameInfo.setId(2);
        assertTrue(gameInfo.getId() == 2);
    }

    @Test
    void testSetName(){
        GameInfo gameInfo = new GameInfo(1, "Test", null);
        gameInfo.setName("Update");
        assertTrue(gameInfo.getName().equals("Update"));
    }

    @Test
    void testSetConnectedPlayers(){
        GameInfo gameInfo = new GameInfo(1, "Test", null);
        List<PlayerData> players = new ArrayList<>();
        players.add(new PlayerData(null, "Test", "ID1", -1));
        gameInfo.setConnectedPlayers(players);
        assertTrue(gameInfo.getConnectedPlayers().get(0).getId().equals("ID1"));
    }
}
