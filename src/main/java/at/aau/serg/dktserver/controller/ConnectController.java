package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public class ConnectController {

    private WebSocketHandlerImpl webSocket;

    public ConnectController(){
        webSocket = WebSocketHandlerImpl.getInstance();
    }
    public void connectUser(PlayerData player,  int gameId, WebSocketSession session){
        List<String> playerIds = webSocket.getPlayerIds();

        if (playerIds.contains(player.getId())){
            System.out.println(String.format("Player: %s tries reconnecting to server...", player.getUsername()));
            webSocket.setSessionOfPlayer(player.getId(), session);
            webSocket.reconnectPlayer(player.getId());
            return;
        }
        webSocket.connectAndAddPlayer(player, gameId, session);
    }
}
