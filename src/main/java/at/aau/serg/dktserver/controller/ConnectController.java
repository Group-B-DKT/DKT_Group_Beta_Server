package at.aau.serg.dktserver.controller;

import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public class ConnectController {

    private WebSocketHandlerImpl webSocket;

    public ConnectController(){
        webSocket = webSocket.getInstance();
    }
    public void connectUser(String username, String playerId,  int gameId, WebSocketSession session){
        List<String> playerIds = webSocket.getPlayerIds();

        if (playerIds.contains(playerId)){
            System.out.println(String.format("Player: %s tries reconnecting to server...", username));
            webSocket.setSessionOfPlayer(playerId, session);
            webSocket.reconnectPlayer(playerId);
            return;
        }
        webSocket.connectAndAddPlayer(username, gameId, playerId, session);
    }
}
