package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.model.interfaces.GameHandler;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Game implements GameHandler {
    public static final int maxPlayer = 6;
    private SecureRandom rng;
    @Getter
    private ArrayList<PlayerData> players;
    private PlayerData host;
    @Getter
    private boolean isStarted = false;
    private PlayerData currentPlayer;
    @Getter
    private String name;
    private ArrayList<Field> fields = new ArrayList<>();
    @Getter
    private int id;

    public Game(int id, PlayerData host, String gameName) {

        this.id = id;
        this.host = host;
        this.name = gameName;
        rng = new SecureRandom();
        players = new ArrayList<>();
        players.add(host);
    }


    @Override
    public int roll_dice(){
        return rng.nextInt(6)+1;
    }
    public int roll_dice(PlayerData playerData) {
        int dice = rng.nextInt(6)+1;
        movePlayer(playerData, dice);
        return dice;
    }

    private PlayerData movePlayer(PlayerData playerData, int dice) {
        int newField = playerData.getCurrentField().getId() + dice;
        newField = newField > playerData.getCurrentField().getId() ? newField % playerData.getCurrentField().getId() + 1 : newField;
        playerData.setCurrentField(fields.get(newField));
        return playerData;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return isStarted == game.isStarted && id == game.id && Objects.equals(rng, game.rng) && Objects.equals(players, game.players) && Objects.equals(host, game.host) && Objects.equals(currentPlayer, game.currentPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rng, players, host, isStarted, currentPlayer, id);
    }
}
