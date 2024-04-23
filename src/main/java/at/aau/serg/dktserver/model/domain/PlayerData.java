package at.aau.serg.dktserver.model.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Objects;

@Getter
public class PlayerData {
    @Setter
    private WebSocketSession session;
    @Setter
    private int gameId;
    private String username;
    private String playerId;
    @Setter
    private boolean isReady;
    @Setter
    private Field currentField;
    @Setter
    private boolean isConnected;
    @Setter
    private int money;

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

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return gameId == that.gameId && isReady == that.isReady && isConnected == that.isConnected && Objects.equals(session, that.session) && Objects.equals(username, that.username) && Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, gameId, username, playerId, isReady, isConnected);
    }

}
