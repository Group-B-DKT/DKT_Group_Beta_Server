package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.model.Game;

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
        games.add(new Game(1));
    }


    public Game getGameById(int id){
        return games.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
    }
}
