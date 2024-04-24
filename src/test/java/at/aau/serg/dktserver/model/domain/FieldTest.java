package at.aau.serg.dktserver.model.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {
    PlayerData playerData;
    @Mock
    WebSocketSession socketSession;
    @BeforeEach
    void setUp() {
        playerData = new PlayerData(socketSession, "Player 1", "1", 1);
    }

    @Test
    void getName() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertEquals("Example 1", f1.getName());
    }

    @Test
    void enterField() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertEquals(playerData.getId(), f1.enterField(playerData));
    }

    @Test
    void getId() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertEquals(1, f1.getId());
    }

    @Test
    void getPrice() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertEquals(100, f1.getPrice());
    }

    @Test
    void getOwner() {
        Field f1 = new Field(1, "Example 1", 100, true);
        f1.setOwner(playerData);
        assertEquals(playerData, f1.getOwner());
    }

    @Test
    void getOwnable() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertTrue(f1.getOwnable());
    }
    @Test
    void getOwnableFalse() {
        Field f1 = new Field(1, "Start", false);
        assertFalse(f1.getOwnable());
    }

}