package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {
    GameManager gameManager;
    PlayerData playerData;
    PlayerData playerData1;
    Game game;
    @Mock
    WebSocketSession webSocketSession;
    @BeforeEach
    void setUp() {
        gameManager = GameManager.getInstance();
        playerData = new PlayerData(webSocketSession, "Test", "1", 1);

        playerData1 = new PlayerData(webSocketSession, "Test 2", "2", 0);
        game = new Game(1, playerData);
        gameManager.setGames(new ArrayList<>());
    }

    @Test
    void getInstance() {
        assertEquals(gameManager, GameManager.getInstance());
    }

    @Test
    void createGame() {
        gameManager.createGame(playerData);
        System.out.println(gameManager.getGames());
        assertEquals(1, gameManager.getGames().size());
    }
    @Test
    void create2Games() {
        gameManager.createGame(playerData);
        gameManager.createGame(playerData1);
        System.out.println(gameManager.getGames());
        assertEquals(2, gameManager.getGames().size());
    }

    @Test
    void joinGame() {
        PlayerData playerData1 = new PlayerData(webSocketSession, "Test 2", "2", 0);
        gameManager.createGame(playerData);
        gameManager.joinGame(1, playerData1);
        assertEquals(2, gameManager.getPlayerNames(1).size());

    }

    @Test
    void getGameById() {
        int id = gameManager.createGame(playerData);
        assertEquals(id, gameManager.getGameById(id).getId());
    }

    @Test
    void getGamesAndPlayerCount() {
        gameManager.createGame(playerData);
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        assertEquals(map, gameManager.getGamesAndPlayerCount());
    }

    @Test
    void getPlayerNames() {
        gameManager.createGame(playerData);
        List<String> names = new ArrayList<>();
        names.add(playerData.getUsername());
        assertEquals(names, gameManager.getPlayerNames(1));
    }

    @Test
    void getFreeGamesAndPlayerCount() {
        gameManager.createGame(playerData);
        for(int i=0; i<5; i++) {
            gameManager.joinGame(1, new PlayerData(webSocketSession, "User nr: " + i, Integer.toString(i), 1));
        }
        Map<Integer, Integer> map = new HashMap<>();
        assertEquals(map, gameManager.getFreeGamesAndPlayerCount());
    }
}