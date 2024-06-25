package at.aau.serg.dktserver.model.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class PlayerData implements Serializable {
    private static final int START_MONEY = 1500;
    @Setter
    @Getter
    private Card currentCard;
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

    @Getter
    @Setter
    private boolean isOnTurn;

    @Setter
    @Getter
    private int color;


    @Setter
    @Getter
    private int currentPosition;

    @Getter
    @Setter
    private boolean hasCheated = false;

    @Getter
    @Setter
    private ArrayList<JokerCard> jokerCards;


    public PlayerData(WebSocketSession session, String username, String playerId, int gameId) {
        this.session = session;
        this.username = username;
        this.id = playerId;
        this.gameId = gameId;
        this.isHost = false;
        this.isReady = false;
        this.money = START_MONEY;
        this.currentPosition = 0;
        this.currentCard = null;
        this.jokerCards = new ArrayList<>();
    }

    public PlayerData(){}

    public void sendMsg(String msg) throws IOException {
        if (!isConnected) return;
        session.sendMessage(new TextMessage(msg));
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
        return Objects.equals(username, that.username) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, gameId, username, id, isReady, isConnected);
    }

    public void copyFrom(PlayerData fromPlayer) {
        gameId = fromPlayer.gameId;
        username = fromPlayer.username;
        id = fromPlayer.id;
        isReady = fromPlayer.isReady;
        currentField = fromPlayer.currentField;
        isConnected = fromPlayer.isConnected;
        money = fromPlayer.money;
        isHost = fromPlayer.isHost;
        isOnTurn = fromPlayer.isOnTurn;
        color = fromPlayer.color;
        currentPosition = fromPlayer.currentPosition;
        hasCheated = fromPlayer.hasCheated;
    }
}
