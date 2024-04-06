package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.PlayerData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    @Getter
    @Setter
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
    public int createGame(PlayerData host, String gameName) {
        Game game = new Game(getFreeId(), host, gameName);
        games.add(game);
        return game.getId();
    }

    public void joinGame(int gameId, PlayerData player) {
        getGameById(gameId).joinGame(player);
    }

    public Game getGameById(int id){
        return games.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
    }

    public Map<Integer, Integer> getGamesAndPlayerCount() {
        Map<Integer, Integer> gamesAndPlayer = new HashMap<>();
        for(Game g: games) {
            gamesAndPlayer.put(g.getId(), g.getPlayers().size());
        }
        return gamesAndPlayer;
    }
    public Map<Integer, Integer> getFreeGamesAndPlayerCount() {
        Map<Integer, Integer> gamesAndPlayer = new HashMap<>();
        for(Game g: games) {
            if (g.getPlayers().size() < Game.maxPlayer) {
                gamesAndPlayer.put(g.getId(), g.getPlayers().size());
            }
        }
        return gamesAndPlayer;
    }
    public List<String> getPlayerNames(int gameId) {
        List<String> players = new ArrayList<>();
        Game game = getGameById(gameId);
        for(PlayerData playerData: game.getPlayers()) {
            players.add(playerData.getUsername());
        }
        return players;
    }

    private int getFreeId() {
        int id = 1;
        boolean isFree;
        while (true) {
            isFree = true;
            for (Game g: games) {
                if (g.getId() == id) {
                    isFree = false;
                    break;
                }
            }
            if(isFree) break;
            id++;
        }
        return id;
    }
}
