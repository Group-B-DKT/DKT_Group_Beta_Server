package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.model.interfaces.GameHandler;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Game implements GameHandler {
    public static final int MAX_PLAYER = 6;

    private static final int NUMBER_OF_FIELDS = 30;

    public static final List<Integer> PLAYER_COLORS = List.of(
            0xFF66FF66, // Hellgrün
            0xFFFF6666, // Hellrot
            0xFF6666FF, // Hellblau
            0xFFFFFF99, // Hellgelb
            0xFFFFCC66, // Hellorange
            0xFFCC99FF  // Hellviolett
    );
    private SecureRandom rng;
    @Getter
    private ArrayList<PlayerData> players;
    private PlayerData host;
    @Getter
    @Setter
    private boolean isStarted = false;
    @Getter
    @Setter
    private PlayerData currentPlayer;
    @Getter
    private String name;
    @Getter
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

    @Override
    public void start(PlayerData player) {
        if(!isStarted && player.equals(host)) {
            isStarted = true;
            setOrder();
            currentPlayer = players.get(0);
            for(PlayerData p: players) {
                p.setCurrentField(fields.get(0));
            }
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
    public boolean buyField(int fieldId, PlayerData playerData) {
        if(fields.get(fieldId).getPrice() > playerData.getMoney()) {
            return false;
        }
        else {
            fields.get(fieldId).setOwner(playerData);
            playerData.setMoney(playerData.getMoney()-fields.get(fieldId).getPrice());
            return true;
        }
    }

    @Override
    public void setFields(ArrayList<Field> fields) {
        this.fields = fields;
    }
    @Override
    public PlayerData removePlayerAndChangeHost(PlayerData player) {
        PlayerData player1 = WebSocketHandlerImpl.getInstance().getPlayerByPlayerId(player.getId());
        players.remove(player1);
        if(player1.equals(host)) {
            player1.setHost(false);
            if(!players.isEmpty()) {
                host = players.get(0);
                host.setHost(true);
            } else {
                host = null;
            }
        }
        return host;
    }

    @Override
    public boolean removePlayer(PlayerData player){
        if (this.players == null) return false;

        return this.players.remove(player);
    }

    @Override
    public void removeFieldOwner(String playerId) {
        this.fields.stream().forEach(f -> {
            if (f.getOwner() != null && f.getOwner().getId().equals(playerId)){
                f.setOwner(null);
            }
        });
    }

    @Override
    public boolean isOnTurn(String fromPlayerId) {
        PlayerData player = this.players.stream()
                .filter(p -> p.getId().equals(fromPlayerId))
                .findAny()
                .orElse(null);
        if (player == null) return false;
        return player.isOnTurn();
    }

    @Override
    public PlayerData getNewHost(){
        PlayerData newHost = null;
        for (PlayerData player: this.players) {
            if (player.isHost()){
                player.setHost(false);
                continue;
            }
            newHost = player;
        }
        if (newHost != null) {
            newHost.setHost(true);
        }
        this.host = newHost;

        return newHost;
    }


    @Override
    public void updateField(Field field) {
        Field savedField = this.fields.stream()
                                      .filter(f -> f.getId() == field.getId())
                                      .findAny().orElse(null);
        if (savedField == null){
            this.fields.add(field);
        }else{
            int index = this.fields.indexOf(savedField);
            this.fields.set(index, field);
        }
    }

    @Override
    public boolean updatePlayer(PlayerData player){
        PlayerData searchPlayer = getPlayers().stream()
                .filter(playerData -> playerData.getId().equals(player.getId()))
                .findFirst().orElse(null);

        if(searchPlayer == null){
            return false;
        }

        searchPlayer.copyFrom(player);
         return true;
    }

    public List<Field> getFields () {
        return fields;

    }

    @Override
    public int getFreePlayerColor() {
        List<Integer> freeColors = new ArrayList<>(PLAYER_COLORS);
        for (PlayerData p: players) {
            freeColors.remove((Integer) p.getColor());
        }
        Collections.shuffle(freeColors);
        return !freeColors.isEmpty() ? freeColors.get(0) : -1;
    }


    public boolean setPlayerPosition(String playerId, int amount){

        PlayerData player = getPlayers().stream()
                .filter(playerData -> playerData.getId().equals(playerId))
                .findFirst().orElse(null);

        int newPostion = player.getCurrentPosition() + amount;

        if(newPostion >= NUMBER_OF_FIELDS){
            newPostion -= NUMBER_OF_FIELDS;
        }
        player.setCurrentPosition(newPostion);

        return true;

    }

    @Override
    public PlayerData getNextPlayer(PlayerData playerByUsername) {
        playerByUsername.setOnTurn(false);
        int index = this.players.indexOf(playerByUsername);
        if (index + 1 >= this.players.size()){
            return this.players.get(0);
        }
        return this.players.get(index + 1);
    }

    @Override
    public void goToPrison(PlayerData player) {
        player.setCurrentField(fields.stream().filter(field -> Objects.equals(field.getName(), "Knast")).findFirst().orElse(null));
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
