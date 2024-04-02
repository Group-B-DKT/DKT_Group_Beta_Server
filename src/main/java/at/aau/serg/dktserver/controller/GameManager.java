package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private List<Game> games;
    private static GameManager gameManager;

    public static GameManager getInstance(){
        if (gameManager != null) return gameManager;
        gameManager = new GameManager();
        return gameManager;
    }

    public GameManager(){
        if (gameManager == null) gameManager = this;
        games = new ArrayList<>();
    }
    public void createGame(PlayerData host) {
        Game game = new Game(getFreeId(), host);
        games.add(game);
    }

    public void joinGame(int gameId, PlayerData player) {
        getGameById(gameId).joinGame(player);
    }
    private int getFreeId() {
        int id = 1;
        boolean isFree = false;
        while (!isFree) {
            isFree = true;
            for (Game g: games) {
                if (g.getId() == id) {
                    isFree = false;
                    break;
                }
            }
            id++;
        }
        return id;
    }
    public Game getGameById(int id){
        return games.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
    }
}
