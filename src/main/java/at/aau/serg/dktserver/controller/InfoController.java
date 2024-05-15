package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.communication.InfoJsonObject;
import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.communication.enums.Info;
import at.aau.serg.dktserver.communication.enums.Request;
import at.aau.serg.dktserver.communication.utilities.WrapperHelper;
import at.aau.serg.dktserver.model.domain.GameInfo;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InfoController {
    private GameManager gameManager;
    private WebSocketHandlerImpl webSocket;
    private Gson gson;

    public InfoController(){
        this.gameManager = GameManager.getInstance();
        this.webSocket = WebSocketHandlerImpl.getInstance();
        this.gson = new Gson();
    }
    public void receiveInfo(Info info, int gameId, String fromPlayerId){
        switch (info){
            case GAME_LIST -> receiveGameList(fromPlayerId);
            case CONNECTED_PLAYERNAMES -> receiveConnectedPlayers(gameId, fromPlayerId);
        }
    }


    private void receiveGameList(String fromPlayerId){
        System.out.println("receiveGameList() -> called!");

        List<GameInfo> gameInfos = gameManager.getGamesAndPlayerCount2();

        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.GAME_LIST, gameInfos);
        PlayerData playerData = webSocket.getPlayerByPlayerId(fromPlayerId);
        Wrapper wrapper = new Wrapper(infoJsonObject.getClass().getSimpleName(), playerData == null ? -1 : playerData.getGameId(), Request.INFO, infoJsonObject);

        webSocket.sendToUser(fromPlayerId, gson.toJson(wrapper));
    }

    private void receiveConnectedPlayers(int gameId, String fromPlayerId) {
        List<GameInfo> gameInfos = new ArrayList<>();
        GameInfo gameInfo = new GameInfo(gameId, null, webSocket.getPLayersByGameId(gameId), gameManager.getGameById(gameId).isStarted());
        gameInfos.add(gameInfo);
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.CONNECTED_PLAYERNAMES, gameInfos);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.INFO, infoJsonObject);

        webSocket.sendToUser(fromPlayerId, msg);
    }
}
