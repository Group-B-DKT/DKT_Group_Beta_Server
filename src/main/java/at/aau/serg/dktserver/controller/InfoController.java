package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.communication.enums.Info;
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
    public void receiveInfo(Info info, int gameId){
        switch (info){
            case GAME_LIST -> receiveGameList();
        }
    }

    private void receiveGameList(){
        System.out.println("receiveGameList() -> called!");
        Map<Integer, Integer> gameInfo = gameManager.getGamesAndPlayerCount();
        webSocket.sendMessage(1, "Test");
    }

}
