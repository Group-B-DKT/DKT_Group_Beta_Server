package at.aau.serg.dktserver;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.ConnectJsonObject;
import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.communication.enums.ConnectType;
import at.aau.serg.dktserver.communication.enums.Request;
import at.aau.serg.dktserver.communication.utilities.WrapperHelper;
import at.aau.serg.dktserver.websocket.WebSocketHandlerClientImpl;
import com.google.gson.Gson;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketHandlerIntegrationTest {

    @LocalServerPort
    private int port;

    private final String WEBSOCKET_URI = "ws://localhost:%d/websocket-example-handler";
    private static Gson gson = new Gson();

    /**
     * Queue of messages from the server.
     */
    BlockingQueue<String> messages = new LinkedBlockingDeque<>();

    @Test
    public void testWebSocketHandlerConnect() throws Exception {
        WebSocketSession session = initStompSession();

        // send a message to the server
        connectToWebsocket(session);

        String response = messages.poll(1, TimeUnit.SECONDS);
        ConnectJsonObject connectJsonObjectReceived = (ConnectJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(connectJsonObjectReceived.getConnectType().equals(ConnectType.CONNECTION_ESTABLISHED));
    }

    @Test
    public void testWebSocketHandlerActionRollDice() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.ROLL_DICE, null, null);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), 1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        int number = Integer.parseInt(actionJsonObjectReceived.getParam());
        assertThat(1 >= number && number <= 6);
    }

    private void connectToWebsocket(WebSocketSession session) throws IOException {
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, "ID1", "Player1");
        Wrapper wrapper = new Wrapper(connectJsonObject.getClass().getSimpleName(), 1, Request.CONNECT, connectJsonObject);
        String msg = gson.toJson(wrapper);
        session.sendMessage(new TextMessage(msg));
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
