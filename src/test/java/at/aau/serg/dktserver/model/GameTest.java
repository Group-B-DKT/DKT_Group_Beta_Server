package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    Game game;
    PlayerData playerData;
    @Mock
    WebSocketSession webSocketSession;
    ArrayList<Field> fields = new ArrayList<>();
    @BeforeEach
    void setUp() {
        playerData = new PlayerData(webSocketSession, "Example", "1", 1);
        game = new Game(1, playerData, "");
        fields.add(new Field(1, "Start", false));
        fields.add(new Field(2, "Example 1", 100, true));
        fields.add(new Field(3, "Example 2", 120, true));
        fields.add(new Field(4, "Example 3", 150, true));
        fields.add(new Field(5, "Example 4", 200, true));
        fields.add(new Field(6, "Example 5", 220, true));
        fields.add(new Field(7, "Example 6", 400, true));
        game.setFields(fields);
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
    void startWrongUser() {
        game.start(new PlayerData(webSocketSession, "False", "2", 1));
        assertFalse(game.isStarted());
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
    public void testGetFreePlayerColorCorrect() {
        int freeColor1 = game.getFreePlayerColor();
        assertTrue(freeColor1 != -1);
    }

    @Test
    public void testGetFreePlayerColorFailed() {
        for (int i = 0; i < 6; i++) {
            PlayerData player = new PlayerData(null, "User" + i, "ID" + i, game.getId());
            player.setColor(game.getFreePlayerColor());
            game.joinGame(player);
        }
        int freeColor1 = game.getFreePlayerColor();
        assertTrue(freeColor1 == -1);
    }

    @Test
    void getId() {
        assertEquals(1, game.getId());
    }
    @Test
    void testEquals() {
        Game game1 = game;
        assertEquals(game, game1);
    }
    @Test
    void testNotEquals() {
        Game game1 = new Game(2, new PlayerData(webSocketSession, "ABC", "1", 2), "");
        assertNotEquals(game, game1);
    }

    @Test
    void buyField() {
        Field f = fields.get(1);
        playerData.setMoney(1500);
        game.setFields(fields);
        assertTrue(game.buyField(f.getId(), playerData));
    }
    @Test
    void buyFieldNoMoney() {
        Field f = fields.get(1);
        playerData.setMoney(50);
        game.setFields(fields);
        assertFalse(game.buyField(f.getId(), playerData));
    }

    @Test
    void testRemoveFieldOwner(){
        PlayerData player = new PlayerData(null, "P1", "ID1", 1000);
        game.joinGame(player);

        game.buyField(0, player);
        game.buyField(1, player);
        game.buyField(2, player);

        game.removeFieldOwner(player.getId());

        Set<Field> playerFields = game.getFields().stream()
                .filter(f -> f.getOwner() != null && f.getOwner().getId().equals(player.getId()))
                .collect(Collectors.toSet());
        assertEquals(0, playerFields.size());
    }

}