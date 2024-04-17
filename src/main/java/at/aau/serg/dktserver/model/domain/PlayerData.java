package at.aau.serg.dktserver.model.domain;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

@ToString
public class PlayerData implements Serializable {
    @Getter
    @Setter
    private transient WebSocketSession session;

    @Getter
    @Setter
    private int gameId;
    @Getter
    private String username;

    @Getter
    private String playerId;
    @Getter
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

    public PlayerData(){}

    public void sendMsg(String msg) throws IOException {
        if (!isConnected) return;
        session.sendMessage(new TextMessage(msg));
    }

    public WebSocketSession getSession() {
        return session;
    }

    public String getUsername() {
        return username;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setConnected(boolean b) {
        isConnected = b;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
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
