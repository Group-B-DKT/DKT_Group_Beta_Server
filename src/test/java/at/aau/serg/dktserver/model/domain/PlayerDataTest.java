package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDataTest {
    Game game;
    PlayerData playerData;
    @Mock
    WebSocketSession webSocketSession;
    ArrayList<Field> fields = new ArrayList<>();
    @BeforeEach
    void setUp() {
        playerData = new PlayerData(webSocketSession, "Example", "1", 1);
        playerData.setMoney(1500);
        fields.add(new Field(1, "Start", false));
        fields.add(new Field(2, "Example 1", 100, true));
        fields.add(new Field(3, "Example 2", 120, true));
        fields.add(new Field(4, "Example 3", 150, true));
        fields.add(new Field(5, "Example 4", 200, true));
        fields.add(new Field(6, "Example 5", 220, true));
        fields.add(new Field(7, "Example 6", 400, true));
        playerData.setCurrentField(fields.get(0));
        game = new Game(1, playerData, "Game");
        game.setFields(fields);
    }
    @Test
    void getUsername() {
        assertEquals("Example", playerData.getUsername());
    }

    @Test
    void getPlayerId() {
        assertEquals("1", playerData.getId());
    }

    @Test
    void getGameId() {
        assertEquals(game.getId(), playerData.getGameId());
    }

    @Test
    void setConnected() {
        playerData.setConnected(true);
        assertTrue(playerData.isConnected());
    }

    @Test
    void isConnected() {
        assertFalse(playerData.isConnected());
    }

    @Test
    void getCurrentField() {
        assertEquals(fields.get(0), playerData.getCurrentField());
    }

    @Test
    void setCurrentField() {
        playerData.setCurrentField(fields.get(3));
        assertEquals(fields.get(3), playerData.getCurrentField());
    }

    @Test
    void getMoney() {
        assertEquals(1500, playerData.getMoney());
    }

    @Test
    void setMoney() {
        int money = 1000;
        playerData.setMoney(money);
        assertEquals(money, playerData.getMoney());
    }

    @Test
    void setRoundsToSkip(){
        playerData.setRoundsToSkip(3);
        assertEquals(3, playerData.getRoundsToSkip());
    }
}