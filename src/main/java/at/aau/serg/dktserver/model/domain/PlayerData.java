package at.aau.serg.dktserver.model.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;


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
    private String id;
    @Getter
    @Setter
    private boolean isReady;
    @Getter
    @Setter
    private Field currentField;
    @Setter
    private boolean isConnected;
    @Getter
    @Setter
    private int money;

    @Getter
    @Setter
    private boolean isHost;

    public PlayerData(WebSocketSession session, String username, String playerId, int gameId) {
        this.session = session;
        this.username = username;
        this.id = playerId;
        this.gameId = gameId;
        this.isHost = false;
        this.isReady = false;
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

    public String getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return gameId == that.gameId && isReady == that.isReady && isConnected == that.isConnected && Objects.equals(session, that.session) && Objects.equals(username, that.username) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, gameId, username, id, isReady, isConnected);
    }

}
