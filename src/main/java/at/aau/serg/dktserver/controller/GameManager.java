package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.GameInfo;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
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
        host.setHost(true);
        Game game = new Game(getFreeId(), host, gameName);
        games.add(game);
        host.setGameId(game.getId());
        return game.getId();
    }
    public PlayerData leaveGame(int gameId, PlayerData player) {
        PlayerData newHost = null;
        Game game = getGameById(gameId);
        if (game != null) {
            newHost = game.removePlayer(player);
            player.setGameId(-1);
            player.setReady(false);
        }else{
            System.err.println("Spiel mit der ID " + gameId + " wurde nicht gefunden.");
        }
        return newHost;
    }

    public void joinGame(int gameId, PlayerData player) {
        getGameById(gameId).joinGame(player);
        player.setGameId(gameId);
    }

    public Game getGameById(int id){
        return games.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
    }
    public boolean removeGame(int gameId) {
        boolean result = false;
        Game game = getGameById(gameId);
        if(game!=null) {
            result = this.games.remove(game);
        }
        return result;

    }

    public void updateField(int gameId, Field field){
        Game game = getGameById(gameId);
        game.updateField(field);
    }


    public List<GameInfo> getGamesAndPlayerCount2() {
        List<GameInfo> gamesAndPlayer = new ArrayList<>();
        for(Game g: games) {
            gamesAndPlayer.add(
                    new GameInfo(g.getId(),g.getName(), getPlayers(g.getId()))
            );
        }
        return gamesAndPlayer;
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
        if(game != null) {
        for(PlayerData playerData: game.getPlayers()) {
            players.add(playerData.getUsername());
        }
        }
        return players;
    }

    public List<PlayerData> getPlayers(int gameId) {
        List<PlayerData> players = new ArrayList<>();
        Game game = getGameById(gameId);
        for(PlayerData playerData: game.getPlayers()) {
            players.add(playerData);
        }
        return players;
    }

    public boolean setIsReady(PlayerData fromPlayer){
        PlayerData player = WebSocketHandlerImpl.getInstance().getPlayerByUsername(fromPlayer.getUsername());
        player.setReady(fromPlayer.isReady());
        return player.isReady();
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
