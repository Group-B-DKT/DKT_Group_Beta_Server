package at.aau.serg.dktserver;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.ConnectJsonObject;
import at.aau.serg.dktserver.communication.InfoJsonObject;
import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.communication.enums.ConnectType;
import at.aau.serg.dktserver.communication.enums.Info;
import at.aau.serg.dktserver.communication.enums.Request;
import at.aau.serg.dktserver.communication.utilities.WrapperHelper;
import at.aau.serg.dktserver.controller.GameManager;
import at.aau.serg.dktserver.model.domain.GameInfo;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.websocket.WebSocketHandlerClientImpl;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketHandlerIntegrationTest {
    private static int id = 1;

    @LocalServerPort
    private int port;

    private final String WEBSOCKET_URI = "ws://localhost:%d/websocket-example-handler";
    private static Gson gson = new Gson();

    /**
     * Queue of messages from the server.
     */
    BlockingQueue<String> messages = new LinkedBlockingDeque<>();

//    @BeforeAll
//    public static void setUp(){
//        GameManager.getInstance().createGame(new PlayerData(null, "", "", -1), "Game1");
//    }


    @Test
    public void testWebSocketHandlerConnect() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, 1);

        String response = messages.poll(1, TimeUnit.SECONDS);
        ConnectJsonObject connectJsonObjectReceived = (ConnectJsonObject) WrapperHelper.getInstanceFromJson(response);
        messages.clear();
        assertThat(connectJsonObjectReceived.getConnectType().equals(ConnectType.CONNECTION_ESTABLISHED)).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionRollDice() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);
        String response = messages.poll(1, TimeUnit.SECONDS);

        GameManager.getInstance().createGame(new PlayerData(null, "", "", -1), "Game1");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, null);
        String msg = WrapperHelper.toJsonFromObject(1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);

        ActionJsonObject actionJsonObject2 = new ActionJsonObject(Action.ROLL_DICE);
        msg = WrapperHelper.toJsonFromObject(1, Request.ACTION, actionJsonObject2);
        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        int number = Integer.parseInt(actionJsonObjectReceived.getParam());
        messages.clear();

        assertThat(1 <= number && number <= 6).isTrue();
    }

    @Test
    public void testWebSocketHandlerInfoGameInfo() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, 1);
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.GAME_LIST, null);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.INFO, infoJsonObject);

        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);
        InfoJsonObject infoJsonObject1 = (InfoJsonObject) WrapperHelper.getInstanceFromJson(response);
        messages.clear();

        assertThat(infoJsonObject1.getGameInfoList().isEmpty()).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionCreateGame() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, "TEST", null);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), -1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction() == Action.GAME_CREATED_SUCCESSFULLY).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionJoinGame() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);

        GameManager.getInstance().createGame(new PlayerData(null, "User", "ID1", -1), "MyGame");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), 1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction() == Action.GAME_JOINED_SUCCESSFULLY).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionSetReady() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        PlayerData player = new PlayerData(null, username, null, -1);
        player.setReady(true);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.READY, null, player);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), 1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);
        System.out.println(msg);
        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getFromPlayer().isReady()).isTrue();
    }

    @Test
    public void testWebSocketHandlerReceiveConnectedPlayers() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        String response = messages.poll(1, TimeUnit.SECONDS);

        GameManager.getInstance().createGame(new PlayerData(null, "User", "ID1", -1), "MyGame");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), 1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.CONNECTED_PLAYERNAMES, null);
        msg = WrapperHelper.toJsonFromObject(1, Request.INFO, infoJsonObject);

        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);

        InfoJsonObject receivedInfoJsonObject = (InfoJsonObject) WrapperHelper.getInstanceFromJson(response);
        GameInfo gameInfo = receivedInfoJsonObject.getGameInfoList().get(0);

        Set<PlayerData> players = gameInfo.getConnectedPlayers().stream().filter(p -> p.getUsername().equals(username)).collect(Collectors.toSet());
        assertThat(players.size() > 0);
    }

    private String connectToWebsocket(WebSocketSession session, int gameId) throws IOException {
        String username = "Player" + id;
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, "ID" + id, username);
        Wrapper wrapper = new Wrapper(connectJsonObject.getClass().getSimpleName(), gameId, Request.CONNECT, connectJsonObject);
        String msg = gson.toJson(wrapper);
        session.sendMessage(new TextMessage(msg));

        id ++;
        return username;
    }


        /**
         * @return The basic session for the WebSocket connection.
         */
    public WebSocketSession initStompSession() throws Exception {
        WebSocketClient client = new StandardWebSocketClient();

        // connect client to the websocket server
        WebSocketSession session = client.execute(new WebSocketHandlerClientImpl(messages), // pass the message list
                        String.format(WEBSOCKET_URI, port))
                // wait 1 sec for the client to be connected
                .get(1, TimeUnit.SECONDS);

        return session;
    }

}
