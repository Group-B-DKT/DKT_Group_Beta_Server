package at.aau.serg.dktserver.model.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class PlayerData {
    @Getter
    @Setter
    private WebSocketSession session;

    @Getter
    private int gameId;
    @Getter
    private String username;

    @Getter
    private String playerId;
    private boolean isReady;

    @Getter
    @Setter
    private boolean isConnected;


    public PlayerData(WebSocketSession session, String username, String playerId, int gameId) {
        this.session = session;
        this.username = username;
        this.playerId = playerId;
        this.gameId = gameId;
    }

    public void sendMsg(String msg) throws IOException {
        if (!isConnected) return;
        session.sendMessage(new TextMessage(msg));
    }
}
