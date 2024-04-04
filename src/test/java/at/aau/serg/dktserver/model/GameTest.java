package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.domain.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    Game game;
    PlayerData playerData;
    @Mock
    WebSocketSession webSocketSession;
    @BeforeEach
    void setUp() {
        playerData = new PlayerData(webSocketSession, "Example", "1", 1);
        game = new Game(1, playerData);
    }

    @Test
    void roll_dice() {
        int dice = game.roll_dice();
        assertTrue(1 <= dice && dice <= 6);
    }

    @Test
    void start() {
        game.start(playerData);
        assertTrue(game.isStarted());
    }

    @Test
    void joinGame() {
        PlayerData playerData1 = new PlayerData(webSocketSession, "Example 2", "2", 1);
        game.joinGame(playerData1);
        assertEquals(2, game.getPlayers().size());
    }

    @Test
    void getPlayers() {
        List<PlayerData> list = new ArrayList<>();
        list.add(playerData);
        assertEquals(list, game.getPlayers());
    }

    @Test
    void getId() {
        assertEquals(1, game.getId());
    }
}