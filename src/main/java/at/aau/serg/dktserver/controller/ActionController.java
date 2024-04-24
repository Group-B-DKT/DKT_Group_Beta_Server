package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.communication.enums.Request;
import at.aau.serg.dktserver.communication.utilities.WrapperHelper;
import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
import com.google.gson.Gson;

public class ActionController {
    private GameManager gameManager;
    private WebSocketHandlerImpl webSocket;
    private Gson gson;

    public ActionController(){
        this.gameManager = GameManager.getInstance();
        this.webSocket = WebSocketHandlerImpl.getInstance();
        this.gson = new Gson();
    }
    public void callAction(Action action, int gameId, String fromUsername, String param, PlayerData fromPlayer){
        switch (action){
            case ROLL_DICE -> rollDice(gameId, webSocket.getPlayerByUsername(fromUsername));
            case CREATE_GAME -> createGame(webSocket.getPlayerByUsername(fromUsername), param);
            case JOIN_GAME -> joinGame(gameId, fromUsername);
            case READY, NOT_READY -> setReady(fromPlayer);
            /*
            case START_GAME -> gameManager.getGameById(gameId).start(webSocket.getPlayerByUsername(fromUsername));
             */
        }
    }


    private void rollDice(int gameId, PlayerData fromPlayer) {
        Game game = gameManager.getGameById(gameId);

        if (game == null) return;
        int value = game.roll_dice();

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.ROLL_DICE, String.format("%d", value), fromPlayer);
        String json = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        webSocket.sendMessage(gameId, json);
    }

    private void createGame(PlayerData playerByUsername, String param) {
        gameManager.createGame(playerByUsername, param);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.GAME_CREATED_SUCCESSFULLY, null, playerByUsername);
        String msg = WrapperHelper.toJsonFromObject(playerByUsername.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(playerByUsername.getGameId(), msg);
        webSocket.sendMessage(-1, msg);

    }

    private void joinGame(int gameId, String fromUsername){
        PlayerData player = webSocket.getPlayerByUsername(fromUsername);
        gameManager.joinGame(gameId, player);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.GAME_JOINED_SUCCESSFULLY, null, player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(gameId, msg);
    }

    private void setReady(PlayerData fromPlayer) {
        gameManager.setIsReady(fromPlayer);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CHANGED_READY_STATUS, null, fromPlayer);
        String msg = WrapperHelper.toJsonFromObject(fromPlayer.getGameId(), Request.ACTION, actionJsonObject);

        webSocket.sendMessage(fromPlayer.getGameId(), msg);
    }
}
