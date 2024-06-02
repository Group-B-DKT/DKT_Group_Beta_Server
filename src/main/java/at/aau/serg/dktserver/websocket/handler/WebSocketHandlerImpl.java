package at.aau.serg.dktserver.websocket.handler;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.ConnectJsonObject;
import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.communication.enums.ConnectType;
import at.aau.serg.dktserver.communication.enums.Request;
import at.aau.serg.dktserver.communication.utilities.WrapperHelper;
import at.aau.serg.dktserver.controller.GameManager;
import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.parser.JsonInputParser;
import at.aau.serg.dktserver.parser.interfaces.InputParser;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        System.out.println("WebSocketHandlerImpl::handleMessage/ " + message.getPayload());

        inputParser.parseInput(message.getPayload().toString(), session, fromPlayername);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        PlayerData player = this.playerData.stream()
                                           .filter(p -> p.getSession().getId().equals(session.getId()))
                                           .findAny().orElse(null);

        if (player == null) return;
        player.setConnected(false);
        player.setReady(false);

        Game game = GameManager.getInstance().getGameById(player.getGameId());

        if (game != null && !game.isStarted()) {
            ActionJsonObject actionJsonObject = new ActionJsonObject(Action.LEAVE_GAME, null, player);
            String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
            inputParser.parseInput(msg, null, player.getUsername());
        }
        else if (game != null && game.isStarted()){
            ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CONNECTION_LOST, null, player);
            String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
            inputParser.parseInput(msg, null, player.getUsername());
        }

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void connectAndAddPlayer(PlayerData player, int gameId, WebSocketSession session){
        System.out.println(String.format("%s connects to server...", player.getId()));
        player.setSession(session);
        this.playerData.add(player);
        reconnectPlayer(player.getId());
    }

    public void reconnectPlayer(String playerId){
        PlayerData player = this.playerData.stream()
                .filter(p -> p.getId() != null && p.getId().equals(playerId))
                .findAny().orElse(null);
        if (player == null) return;

        String msg = "";
        if (player.getGameId() == -1) {
            msg = setPlayerDefaultsAndCreateMessage(player);

        }else{
            Game game = GameManager.getInstance().getGameById(player.getGameId());
            if (!game.isStarted()) setPlayerDefaultsAndCreateMessage(player);

            setPlayerGameIdAndCreateMessage(player, player.getGameId(), ConnectType.RECONNECT_TO_GAME);
        }

        sendToUser(player.getUsername(), msg);

    }

    private String setPlayerGameIdAndCreateMessage(PlayerData player, int gameId, ConnectType connectType) {
        String msg;
        player.setConnected(true);
        player.setGameId(gameId);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(connectType);
        msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.CONNECT, connectJsonObject);
        return msg;
    }

    private String setPlayerDefaultsAndCreateMessage(PlayerData player) {
        return setPlayerGameIdAndCreateMessage(player, -1, ConnectType.CONNECTION_ESTABLISHED);
    }

    public void sendMessage(int gameId, String msg){
        this.playerData.stream()
                .filter(p -> p.getGameId() == gameId && p.isConnected())
                .forEach(p-> {
                    try {
                        p.sendMsg(msg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void sendToUser(String username, String msg){
        System.out.println("WebSocketHandlerImpl::sendToUser/ " + msg);
        this.playerData.stream()
                .filter(p -> p.getUsername() != null && p.getUsername().equals(username))
                .forEach(p-> {
                    try {
                        p.sendMsg(msg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void setSessionOfPlayer(String playerId, WebSocketSession session){
        this.playerData.stream()
                .filter(p -> p.getId().equals(playerId))
                .forEach(p -> p.setSession(session));
    }

    public List<String> getPlayerIds() {
        return this.playerData.stream().map(PlayerData::getId).toList();
    }


    public static WebSocketHandlerImpl getInstance(){
        if (webSocket != null) return webSocket;
        webSocket = new WebSocketHandlerImpl();
        return webSocket;
    }

    public PlayerData getPlayerByUsername(String fromUsername) {
        return this.playerData.stream().filter(p -> Objects.equals(p.getUsername(), fromUsername)).findFirst().orElse(null);
    }

    public List<PlayerData> getPLayersByGameId(int gameId){
        return this.playerData.stream().filter(p -> p.getGameId() == gameId).collect(Collectors.toList());
    }

    public PlayerData getPlayerByPlayerId(String playerId){
        return this.playerData.stream()
                .filter(p -> p.getId().equals(playerId))
                .findAny().orElse(null);
    }
}
