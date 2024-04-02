package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.model.interfaces.GameHandler;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

public class Game implements GameHandler {
    private SecureRandom rng;
    private ArrayList<PlayerData> players;
    private PlayerData host;
    private boolean isStarted = false;
    private PlayerData currentPlayer;

    @Getter
    private int id;

    public Game(int id, PlayerData host) {

        this.id = id;
        this.host = host;
        rng = new SecureRandom();
    }


    @Override
    public int roll_dice(){
        return rng.nextInt(6)+1;
    }

    @Override
    public void start(PlayerData player) {
        if(!isStarted && player.equals(host)) {
            isStarted = true;
            setOrder();
            currentPlayer = players.get(0);
        }
    }

    @Override
    public void setOrder() {
        Collections.shuffle(players);
    }
    @Override
    public void joinGame(PlayerData player) {
        if(!players.contains(player)) {
            players.add(player);
        }
    }

    public int getId() {
        return id;
    }
}
