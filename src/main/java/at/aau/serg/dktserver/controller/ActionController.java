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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            case LEAVE_GAME -> leaveGame(fromPlayerId);
            case BUY_FIELD -> buyField(fromPlayer, fields.get(0));
            case INIT_FIELDS -> initFields(gameId, param);
            case READY, NOT_READY -> setReady(fromPlayer);
            case GAME_STARTED -> initGame(gameId, fields);
            case MOVE_PLAYER -> movePlayer(webSocket.getPlayerByPlayerId(fromPlayerId), param);
            case END_TURN -> endTurn(webSocket.getPlayerByPlayerId(fromPlayerId));
            case SUBMIT_CHEAT -> submitCheat(webSocket.getPlayerByPlayerId(fromPlayerId));
        }
    }

    private void endTurn(PlayerData playerById) {
        PlayerData playerData = gameManager.getNextPlayer(playerById);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.END_TURN, null, playerData, null);
        String msg = WrapperHelper.toJsonFromObject(playerData.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(playerData.getGameId(), msg);
    }

    private void initGame(int gameId, List<Field> fields) {
        for (Field field: fields) {
            gameManager.updateField(gameId, field);
        }
        gameManager.getGameById(gameId).setStarted(true);
        gameManager.getGameById(gameId).getPlayers().sort(Comparator.comparing(PlayerData::getId));

        SecureRandom random = new SecureRandom();

        List<PlayerData> players = gameManager.getGameById(gameId).getPlayers();
        PlayerData isOnTurnPlayer = players.get(random.nextInt(players.size()));
        isOnTurnPlayer.setOnTurn(true);

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


    private void leaveGame(String fromPlayerId) {
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

        if(positionSet == false){

            return;
        }

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.MOVE_PLAYER, param, player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(player.getGameId(), msg);
    }

    private void submitCheat(PlayerData player) {
        player.setHasCheated(true);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.SUBMIT_CHEAT, "", player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        webSocket.sendMessage(player.getGameId(), msg);
    }
}
