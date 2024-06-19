package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.communication.enums.Request;
import at.aau.serg.dktserver.communication.utilities.WrapperHelper;
import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.*;

public class ActionController {
    private GameManager gameManager;
    private WebSocketHandlerImpl webSocket;
    private Gson gson;

    public ActionController(){
        this.gameManager = GameManager.getInstance();
        this.webSocket = WebSocketHandlerImpl.getInstance();
        this.gson = new Gson();
    }
    public void callAction(Action action, int gameId, String fromPlayerId, String param, PlayerData fromPlayer, List<Field> fields){
        switch (action){
            case ROLL_DICE -> rollDice2(gameId, webSocket.getPlayerByPlayerId(fromPlayerId), param);
            case CREATE_GAME -> createGame(webSocket.getPlayerByPlayerId(fromPlayerId), param);
            case JOIN_GAME -> joinGame(gameId, fromPlayerId);
            case LEAVE_GAME -> leaveLobby(fromPlayerId);
            case CONNECTION_LOST -> leaveGame(gameId, fromPlayerId);
            case BUY_FIELD -> buyField(fromPlayer, fields.get(0));
            case INIT_FIELDS -> initFields(gameId, param);
            case READY, NOT_READY -> setReady(fromPlayer);
            case GAME_STARTED -> initGame(gameId, fields);
            case MOVE_PLAYER -> movePlayer(webSocket.getPlayerByPlayerId(fromPlayerId), param);
            case END_TURN -> endTurn(webSocket.getPlayerByPlayerId(fromPlayerId));
            case UPDATE_MONEY -> updateMoney(fromPlayer, param);
            case PAY_TAXES -> payTaxes(fromPlayer);
            case SUBMIT_CHEAT -> submitCheat(webSocket.getPlayerByPlayerId(fromPlayerId));
            case REPORT_CHEAT -> reportCheat(gameId, fromPlayer, param);
            case RECONNECT_OK -> rejoinPlayer(webSocket.getPlayerByPlayerId(fromPlayerId));
            case RECONNECT_DISCARD -> discardReconnect(Integer.parseInt(param), fromPlayer);
        }
    }

    private void discardReconnect(int gameId, PlayerData fromPlayer) {
        if (!gameManager.removePlayerFromGame(gameId, fromPlayer)){
            return;
        }

        PlayerData playerData = WebSocketHandlerImpl.getInstance().getPlayerByPlayerId(fromPlayer.getId());
        if (playerData == null)
            return;

        boolean wasConnected = playerData.isConnected();
        playerData.copyFrom(fromPlayer);

        if (!wasConnected) playerData.setGameId(-1);
        else playerData.setConnected(true);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.RECONNECT_DISCARD, null, fromPlayer, null);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        webSocket.sendMessage(gameId, msg);
        webSocket.sendToUser(fromPlayer.getId(), msg);
    }

    private void rejoinPlayer(PlayerData player) {
        Game game = gameManager.getGameById(player.getGameId());

        if (game == null) return;

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.RECONNECT_OK, player.getId(), game.getCurrentPlayer(), game.getFields());
        String msg = WrapperHelper.toJsonFromObject(game.getId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(game.getId(), msg);
    }

    private void leaveGame(int gameId, String fromPlayerId) {
        if (fromPlayerId == null){
            return;
        }

        PlayerData player = webSocket.getPlayerByPlayerId(fromPlayerId);

        if (gameManager.isOnTurn(gameId, fromPlayerId))
            endTurn(player);

        PlayerData newHost = null;
        if (player.isHost()) {
            newHost = gameManager.getNewHost(player.getGameId());
            if (newHost == null) {
                player.setGameId(-1);
                gameManager.removeGame(gameId);
                return;
            }

            ActionJsonObject actionJsonObject = new ActionJsonObject(Action.HOST_CHANGED, null, newHost);
            String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
            webSocket.sendMessage(player.getGameId(), msg);
        }



        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CONNECTION_LOST, LocalTime.now().toString(), player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(player.getGameId(), msg);
    }

    private void endTurn(PlayerData playerById) {
        PlayerData playerData = gameManager.getNextPlayer(playerById);
        gameManager.getGameById(playerById.getGameId()).setCurrentPlayer(playerData);
        playerData.setOnTurn(true);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.END_TURN, null, playerData, null);
        String msg = WrapperHelper.toJsonFromObject(playerData.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(playerData.getGameId(), msg);
    }

    private void initGame(int gameId, List<Field> fields) {
        for (Field field: fields) {
            gameManager.updateField(gameId, field);
        }
        Game game = gameManager.getGameById(gameId);
        game.setStarted(true);
        game.getPlayers().sort(Comparator.comparing(PlayerData::getId));

        SecureRandom random = new SecureRandom();

        List<PlayerData> players = gameManager.getGameById(gameId).getPlayers();
        PlayerData isOnTurnPlayer = players.get(random.nextInt(players.size()));
        isOnTurnPlayer.setOnTurn(true);
        game.setCurrentPlayer(isOnTurnPlayer);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.GAME_STARTED, null, isOnTurnPlayer, fields);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        String msg2 = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);

        webSocket.sendMessage(gameId, msg);
        webSocket.sendMessage(-1, msg2);
    }

    private void initFields(int gameId, String param) {
        Game game = gameManager.getGameById(gameId);
        Type listType = new TypeToken<ArrayList<Field>>(){}.getType();
        ArrayList<Field> fields = gson.fromJson(param, listType);
        game.setFields(fields);
    }

    private void rollDice2(int gameId, PlayerData fromPlayer, String param) {
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.ROLL_DICE,param ,fromPlayer);
        String json = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        webSocket.sendMessage(gameId, json);
    }
    private void createGame(PlayerData playerById, String param) {
        int gameId = gameManager.createGame(playerById, param);

        playerById.setColor(gameManager.getGameById(gameId).getFreePlayerColor());

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.GAME_CREATED_SUCCESSFULLY, null, playerById);
        String msg = WrapperHelper.toJsonFromObject(playerById.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(playerById.getGameId(), msg);
        webSocket.sendMessage(-1, msg);

    }

    private void buyField(PlayerData player, Field field) {
        if(player == null) return;
        Game game = gameManager.getGameById(player.getGameId());
        if(game == null) return;

        gameManager.updateField(player.getGameId(), field);
        gameManager.updatePlayer(player.getGameId(), player);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.BUY_FIELD, null, player, Collections.singletonList(field));
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(player.getGameId(), msg);
    }

    private void joinGame(int gameId, String fromPlayerId){
        PlayerData player = webSocket.getPlayerByPlayerId(fromPlayerId);

        if (player == null) return;

        player.setColor(gameManager.getGameById(gameId).getFreePlayerColor());

        gameManager.joinGame(gameId, player);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.GAME_JOINED_SUCCESSFULLY, null, player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(gameId, msg);
        webSocket.sendMessage(-1, msg);
    }


    private void leaveLobby(String fromPlayerId) {
    PlayerData player = webSocket.getPlayerByPlayerId(fromPlayerId);
    if (player == null) return;
    int gameId = player.getGameId();

    PlayerData newHost = gameManager.leaveGame(gameId, player);

    ActionJsonObject actionJsonObject1 = new ActionJsonObject(Action.LEAVE_GAME, null, player);
    String msg1 = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject1);

    webSocket.sendMessage(-1, msg1);
    webSocket.sendMessage(gameId, msg1);

    if(newHost == null) {
        if(gameManager.removeGame(gameId)) {
            ActionJsonObject actionJsonObject2 = new ActionJsonObject(Action.GAME_DELETED, null, null);
            String msg2 = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject2);
            webSocket.sendMessage(-1, msg2);
        }
        return;
    }

    ActionJsonObject actionJsonObject3 = new ActionJsonObject(Action.HOST_CHANGED, null, newHost);
    String msg3 = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject3);
    webSocket.sendMessage(gameId, msg3);


    }

    private void setReady(PlayerData fromPlayer) {
        gameManager.setIsReady(fromPlayer);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CHANGED_READY_STATUS, null, fromPlayer);
        String msg = WrapperHelper.toJsonFromObject(fromPlayer.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(fromPlayer.getGameId(), msg);
    }

    private void movePlayer(PlayerData player, String param){
        int diceResult = Integer.parseInt(param);

        boolean positionSet = gameManager.setPlayerPosition(player.getId(), player.getGameId(), diceResult);

        if(!positionSet){
            return;
        }

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.MOVE_PLAYER, param, player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(player.getGameId(), msg);
    }


    private void updateMoney(PlayerData player, String param){

        boolean moneySet = gameManager.updatePlayer(player.getGameId(), player);

        if(moneySet == false){
            return;
        }
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.UPDATE_MONEY, param, player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(player.getGameId(), msg);

    }
    private void payTaxes(PlayerData player){

        int oldMoney = Objects.requireNonNull(gameManager.getPlayers(player.getGameId()).stream().filter(p -> p.getId().equals(player.getId())).findAny().orElse(null)).getMoney();
        boolean moneySet = gameManager.updatePlayer(player.getGameId(), player);

        if(moneySet == false){
            return;
        }

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.PAY_TAXES,Integer.toString(Math.abs(oldMoney- player.getMoney())) , player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(player.getGameId(), msg);

    }

    private void submitCheat(PlayerData player) {
        player.setHasCheated(true);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.SUBMIT_CHEAT, "", player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(player.getGameId(), msg);
    }


    private void reportCheat(int gameId, PlayerData fromPlayer, String param) {
        Game game = gameManager.getGameById(gameId);
        PlayerData player = game.getPlayers().stream().filter(playerData -> playerData.getId().equals(param)).findFirst().orElse(null);
        if(player.isHasCheated()) {
            player.setMoney(200);
            player.setHasCheated(false);
            game.goToPrison(player);
        }
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.REPORT_CHEAT, param, fromPlayer);
        String msg = WrapperHelper.toJsonFromObject(fromPlayer.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(fromPlayer.getGameId(), msg);
    }

}
