package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.communication.InfoJsonObject;
import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.communication.enums.Info;
import at.aau.serg.dktserver.communication.enums.Request;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
import com.google.gson.Gson;

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
    public void receiveInfo(Info info, int gameId, String fromPlayername){
        switch (info){
            case GAME_LIST -> receiveGameList(fromPlayername);
        }
    }

    private void receiveGameList(String fromPlayername){
        System.out.println("receiveGameList() -> called!");
        Map<Integer, Integer> gameInfo = gameManager.getGamesAndPlayerCount();
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.GAME_LIST, -1, gameInfo);
        Wrapper wrapper = new Wrapper(infoJsonObject.getClass().getSimpleName(), -1, Request.INFO, infoJsonObject);
        webSocket.sendToUser(fromPlayername, gson.toJson(wrapper));
    }

}
