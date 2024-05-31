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
import at.aau.serg.dktserver.model.Game;
import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.GameInfo;
import at.aau.serg.dktserver.model.domain.PlayerData;
import at.aau.serg.dktserver.websocket.WebSocketHandlerClientImpl;
import at.aau.serg.dktserver.websocket.handler.WebSocketHandlerImpl;
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
import java.util.*;
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
    public void testWebSocketHandlerReConnect() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "100";
        PlayerData player = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        session = initStompSession();

        session.sendMessage(new TextMessage(msg));


        response = messages.poll(1, TimeUnit.SECONDS);
        ConnectJsonObject connectJsonObjectReceived = (ConnectJsonObject) WrapperHelper.getInstanceFromJson(response);
        messages.clear();
        assertThat(connectJsonObjectReceived.getConnectType().equals(ConnectType.CONNECTION_ESTABLISHED)).isTrue();
    }

    @Test
    public void testWebSocketHandlerGetPLayerById() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "100";
        PlayerData playerOne = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, playerOne);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        messages.clear();
        PlayerData player = WebSocketHandlerImpl.getInstance().getPlayerByPlayerId(p_id);
        assertThat(player != null && player.getUsername().equals("Player" + p_id)).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionRollDice2() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);
        String response = messages.poll(1, TimeUnit.SECONDS);
        int[] diceResult = {4,5};
        String param = gson.toJson(diceResult);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.ROLL_DICE, param, null);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);

        System.out.println(response);

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        int[] numbers = gson.fromJson(actionJsonObject.getParam(), new int[2].getClass());
        messages.clear();

        assertThat(Arrays.equals(numbers,diceResult)).isTrue();

    }

    @Test
    public void testWebSocketHandlerInfoGameInfo() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, 1);
        GameManager.getInstance().createGame(new PlayerData(), "Test");
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.GAME_LIST, null);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.INFO, infoJsonObject);

        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);
        InfoJsonObject infoJsonObject1 = (InfoJsonObject) WrapperHelper.getInstanceFromJson(response);
        messages.clear();

        assertThat(infoJsonObject1.getGameInfoList().isEmpty()).isFalse();
    }

    @Test
    public void testWebSocketHandlerActionCreateGame() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, "TEST", new PlayerData());
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

        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "User", "ID1", -1), "MyGame");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, new PlayerData());
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), gameId, Request.ACTION, actionJsonObject);
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

        String playerId = connectToWebsocket(session, -1);
        String response = messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "", playerId, -1);
        player.setReady(true);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.READY, null, player);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), 1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);
        session.sendMessage(new TextMessage(msg));

        response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getFromPlayer().isReady()).isTrue();
    }

    @Test
    public void testWebSocketHandlerReceiveConnectedPlayers() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        String response = messages.poll(1, TimeUnit.SECONDS);

        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "User", "ID1", -1), "MyGame");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), gameId, Request.ACTION, actionJsonObject);
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
    @Test
    public void testWebSocketHandlerActionLeaveGame() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "200";
        PlayerData playerOne = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, playerOne);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + p_id, p_id, -1);
        int gameId = GameManager.getInstance().createGame(player, "MyGame");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);

        player.setGameId(gameId);
        actionJsonObject = new ActionJsonObject(Action.LEAVE_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION , actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction() == Action.LEAVE_GAME).isTrue();
        assertThat(GameManager.getInstance().getGameById(gameId).getPlayers().size()==1).isTrue();
    }
    @Test
    public void testWebSocketHandlerActionLeaveGameNoHostLeft() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "300";
        PlayerData playerOne = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, playerOne);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + p_id, p_id, -1);


        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);

        int gameId = actionJsonObjectReceived.getFromPlayer().getGameId();

        player.setGameId(gameId);
        actionJsonObject = new ActionJsonObject(Action.LEAVE_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION , actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);
        actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction() == Action.LEAVE_GAME).isTrue();
        assertThat(GameManager.getInstance().getGameById(gameId) == null).isTrue();
    }
    @Test
    public void testWebSocketHandlerActionInitFields() throws Exception {
        WebSocketSession session = initStompSession();

        ArrayList<Game> games = new ArrayList<>();
        games.add(new Game(1, null, "game"));

        GameManager.getInstance().setGames(games);

        String json = "[{\"id\":1,\"name\":\"Los!\",\"ownable\":false,\"price\":0},{\"id\":2,\"name\":\"Hafnersee\",\"ownable\":true,\"price\":0},{\"id\":3,\"name\":\"Ereignisfeld\",\"ownable\":false,\"price\":0},{\"id\":4,\"name\":\"Pyramidenkogel\",\"ownable\":true,\"price\":0},{\"id\":5,\"name\":\"Reifnitz\",\"ownable\":true,\"price\":0},{\"id\":6,\"name\":\"Dellach\",\"ownable\":true,\"price\":0},{\"id\":7,\"name\":\"Wörtherseeschifffahrt\",\"ownable\":true,\"price\":0},{\"id\":8,\"name\":\"Gemeinschaftsfeld\",\"ownable\":false,\"price\":0},{\"id\":9,\"name\":\"Kathreinkogel\",\"ownable\":true,\"price\":0},{\"id\":10,\"name\":\"Gesetzesverletzung\",\"ownable\":false,\"price\":0},{\"id\":11,\"name\":\"Auen\",\"ownable\":true,\"price\":0},{\"id\":12,\"name\":\"Kraftwerk Forstsee\",\"ownable\":true,\"price\":0},{\"id\":13,\"name\":\"Tierpark Rosegg\",\"ownable\":true,\"price\":0},{\"id\":14,\"name\":\"Casinoplatz\",\"ownable\":true,\"price\":0},{\"id\":15,\"name\":\"Seecorso\",\"ownable\":true,\"price\":0},{\"id\":16,\"name\":\"Gemeinschaftsfeld\",\"ownable\":false,\"price\":0},{\"id\":17,\"name\":\"Töschling\",\"ownable\":true,\"price\":0},{\"id\":18,\"name\":\"Urlaubsgeld\",\"ownable\":false,\"price\":0},{\"id\":19,\"name\":\"St. Martin\",\"ownable\":true,\"price\":0},{\"id\":20,\"name\":\"Ereignisfeld\",\"ownable\":false,\"price\":0},{\"id\":21,\"name\":\"Seeuferstraße\",\"ownable\":true,\"price\":0},{\"id\":22,\"name\":\"Annastraße\",\"ownable\":true,\"price\":0},{\"id\":23,\"name\":\"Gemeinschaftsfeld\",\"ownable\":false,\"price\":0},{\"id\":24,\"name\":\"Koschatweg\",\"ownable\":true,\"price\":0},{\"id\":25,\"name\":\"Knast\",\"ownable\":false,\"price\":0},{\"id\":26,\"name\":\"Römerweg\",\"ownable\":true,\"price\":0},{\"id\":27,\"name\":\"Reptilienzoo\",\"ownable\":true,\"price\":0},{\"id\":28,\"name\":\"Lorettoweg\",\"ownable\":true,\"price\":0},{\"id\":29,\"name\":\"Süduferstraße\",\"ownable\":true,\"price\":0},{\"id\":30,\"name\":\"Lorettoweg\",\"ownable\":true,\"price\":0}]";

        connectToWebsocket(session, 1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.INIT_FIELDS, json, null);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), 1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));


        String response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);
        response = messages.poll(1, TimeUnit.SECONDS);
        GameManager gm = GameManager.getInstance();
        Game game = gm.getInstance().getGameById(1);

        assertThat(game.getFields().size() == 30);

    }

    @Test
    public void testWebSocketHandlerActionInitGame() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        List<Field> fields = List.of(new Field(1, "Field1", false),
                                     new Field(2, "Field2", false),
                                     new Field(3, "Field3", false));

        int gameId = GameManager.getInstance().createGame(new PlayerData(null, username, "ID1", -1), "Game200");
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        actionJsonObject = new ActionJsonObject(Action.GAME_STARTED, null, null, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        System.out.println("111111 " + response);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction() == Action.GAME_STARTED).isTrue();
        assertThat(GameManager.getInstance().getGameById(gameId).getFields().size() == fields.size()).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionBuyField() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        List<Field> fields = List.of(new Field(0, "Field1", true));


        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "U1", "ID1", -1), "Game200");
        player.setGameId(gameId);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);


        actionJsonObject = new ActionJsonObject(Action.GAME_STARTED, null, null, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        fields.get(0).setOwner(player);



        actionJsonObject = new ActionJsonObject(Action.BUY_FIELD, null, player, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        Game game = GameManager.getInstance().getGameById(gameId);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction() == Action.BUY_FIELD).isTrue();
        assertThat(game.getFields().get(0).getOwner().getUsername().equals(player.getUsername()));

    }
    @Test
    public void testWebSocketHandlerActionSubmitCheat() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        List<Field> fields = List.of(new Field(0, "Field1", true));


        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "U1", "ID1", -1), "Game200");
        player.setGameId(gameId);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);


        actionJsonObject = new ActionJsonObject(Action.GAME_STARTED, null, null, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        fields.get(0).setOwner(player);



        actionJsonObject = new ActionJsonObject(Action.SUBMIT_CHEAT, "500", player, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(2, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assert actionJsonObjectReceived != null;
        assertThat(actionJsonObjectReceived.getAction() == Action.SUBMIT_CHEAT).isTrue();
        assertThat(actionJsonObjectReceived.getFromPlayer().isHasCheated()).isTrue();
        assertThat(actionJsonObjectReceived.getFromPlayer().getMoney() - player.getMoney() == 500);

    }

    @Test
    public void testWebSocketHandlerActionSubmitCheatNoParameter() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        List<Field> fields = List.of(new Field(0, "Field1", true));


        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "U1", "ID1", -1), "Game200");
        player.setGameId(gameId);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);


        actionJsonObject = new ActionJsonObject(Action.GAME_STARTED, null, null, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        fields.get(0).setOwner(player);



        actionJsonObject = new ActionJsonObject(Action.SUBMIT_CHEAT, null, player, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(2, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived == null).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionBuyFieldNoGameFound() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.BUY_FIELD, null, null, List.of(new Field(1, "Field1", true)));
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);

        assertThat(response == null).isTrue();
    }



    private String connectToWebsocket(WebSocketSession session, int gameId) throws IOException {
        String username = "Player" + id;
        String playerId = "ID" + id;
        PlayerData playerOne = new PlayerData(null, username , playerId, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, playerOne);
        Wrapper wrapper = new Wrapper(connectJsonObject.getClass().getSimpleName(), gameId, Request.CONNECT, connectJsonObject);
        String msg = gson.toJson(wrapper);
        session.sendMessage(new TextMessage(msg));

        id ++;
        return playerId;
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


    @Test
    public void testWebSocketHandlerActionMovePlayerNotNull() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);

        actionJsonObject = new ActionJsonObject(Action.MOVE_PLAYER, "2", null, null);
        msg = WrapperHelper.toJsonFromObject(actionJsonObjectReceived.getFromPlayer().getGameId(), Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);
        actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);

        assertThat(actionJsonObjectReceived.getParam().equals("2")).isTrue();
    }

    @Test
    public void testWebSocketHandlerActionEndTurn() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "H1", "HI1", -1), "Game10");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, player, null);
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        actionJsonObject = new ActionJsonObject(Action.END_TURN, null, player, null);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction() == Action.END_TURN).isTrue();
    }


}
