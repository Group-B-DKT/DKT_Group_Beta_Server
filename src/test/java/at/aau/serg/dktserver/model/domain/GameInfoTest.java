package at.aau.serg.dktserver.model.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameInfoTest {
    @Test
    void testEqualsTrue(){
        GameInfo gameInfo = new GameInfo(1, "Test", 1);
        GameInfo gameInfo2 = new GameInfo(1, "Test", 1);
        assertTrue(gameInfo.equals(gameInfo2));
    }

    @Test
    void testEqualsFalse(){
        GameInfo gameInfo = new GameInfo(1, "Test", 1);
        GameInfo gameInfo2 = new GameInfo(1, "Test2", 1);
        assertFalse(gameInfo.equals(gameInfo2));
    }

    @Test
    void testHashTrue(){
        GameInfo gameInfo = new GameInfo(1, "Test", 1);
        GameInfo gameInfo2 = new GameInfo(1, "Test", 1);
        assertFalse(gameInfo.hashCode() == gameInfo2.hashCode());
    }

    @Test
    void testHashFalse(){
        GameInfo gameInfo = new GameInfo(1, "Test", 1);
        GameInfo gameInfo2 = new GameInfo(1, "Test2", 1);
        assertFalse(gameInfo.hashCode() == gameInfo2.hashCode());
    }
}
