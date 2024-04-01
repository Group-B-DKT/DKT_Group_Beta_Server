package at.aau.serg.dktserver.websocket.handler;

import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.parser.JsonInputParser;
import at.aau.serg.dktserver.parser.interfaces.InputParser;
import org.springframework.web.socket.*;

import java.util.ArrayList;
import java.util.List;

public class WebSocketHandlerImpl implements WebSocketHandler {
    private List<PlayerData> playerData;

    private static WebSocketHandlerImpl webSocket;
    private InputParser inputParser;

    public WebSocketHandlerImpl(){
        if (webSocket == null) webSocket = this;
        this.inputParser = new JsonInputParser();
        this.playerData = new ArrayList<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        PlayerData fromPlayer = this.playerData.stream()
                .filter(p -> p.getSession().getId().equals(session.getId()))
                .findAny().orElse(null);
        String fromPlayername = fromPlayer != null ? fromPlayer.getUsername() : null;

        inputParser.parseInput(message.getPayload().toString(), session, fromPlayername);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public static WebSocketHandlerImpl getInstance(){
        if (webSocket != null) return webSocket;
        webSocket = new WebSocketHandlerImpl();
        return webSocket;
    }
}
