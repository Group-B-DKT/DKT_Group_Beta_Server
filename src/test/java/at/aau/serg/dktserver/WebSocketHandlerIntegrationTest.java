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
import java.lang.reflect.Type;
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



    @Test
    void testWebSocketHandlerConnect() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, 1);

        String response = messages.poll(1, TimeUnit.SECONDS);
        ConnectJsonObject connectJsonObjectReceived = (ConnectJsonObject) WrapperHelper.getInstanceFromJson(response);
        messages.clear();
        assertThat(connectJsonObjectReceived.getConnectType()).isEqualTo(ConnectType.CONNECTION_ESTABLISHED);
    }


    @Test
    void testWebSocketHandlerReConnect() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "100";
        PlayerData player = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        session = initStompSession();

        session.sendMessage(new TextMessage(msg));


        String response = messages.poll(1, TimeUnit.SECONDS);
        ConnectJsonObject connectJsonObjectReceived = (ConnectJsonObject) WrapperHelper.getInstanceFromJson(response);
        messages.clear();
        assertThat(connectJsonObjectReceived.getConnectType()).isEqualTo(ConnectType.CONNECTION_ESTABLISHED);
    }

    @Test
    void testWebSocketHandlerGetPLayerById() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "100";
        PlayerData playerOne = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, playerOne);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        messages.clear();
        PlayerData player = WebSocketHandlerImpl.getInstance().getPlayerByPlayerId(p_id);
        assertThat(player != null && player.getUsername().equals("Player" + p_id)).isTrue();
    }

    @Test
    void testWebSocketHandlerActionRollDice2() throws Exception {
        WebSocketSession session = initStompSession(); //enable connection
        connectToWebsocket(session, -1); // make connection to lokal test instanz of server

        messages.poll(1, TimeUnit.SECONDS); //needed to get initial connectJsonObject

        //initialize client results
        int[] diceResult = {4,5};
        String param = gson.toJson(diceResult); //gson makes a json-object out of the diceResult

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.ROLL_DICE, param, null); //makes new actionJsonObjekt wih special parameters
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject); //create message json object of previous object with new parameters
        session.sendMessage(new TextMessage(msg)); //sends message to the server
        String response = messages.poll(1, TimeUnit.SECONDS); //response of the server

        System.out.println(response);

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response); //pass json from server response
        int[] numbers = gson.fromJson(actionJsonObjectReceived.getParam(), new int[2].getClass()); //makes params into a new int[] with size 2 -> first argument: String with json format, second argument: desired class
        messages.clear(); //empty message

        assertThat(Arrays.equals(numbers,diceResult)).isTrue();

    }

    @Test
    void testWebSocketHandlerInfoGameInfo() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, 1);
        GameManager.getInstance().createGame(new PlayerData(), "Test");
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.GAME_LIST, null);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.INFO, infoJsonObject);

        session.sendMessage(new TextMessage(msg));

        messages.poll(1, TimeUnit.SECONDS);
        String response = messages.poll(1, TimeUnit.SECONDS);
        InfoJsonObject infoJsonObject1 = (InfoJsonObject) WrapperHelper.getInstanceFromJson(response);
        messages.clear();

        assertThat(infoJsonObject1.getGameInfoList()).isNotEmpty();
    }

    @Test
    void testWebSocketHandlerActionCreateGame() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, "TEST", new PlayerData());
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), -1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));

        messages.poll(1, TimeUnit.SECONDS);
        String response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.GAME_CREATED_SUCCESSFULLY);
    }

    @Test
    void testWebSocketHandlerActionJoinGame() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);

        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "User", "ID1", -1), "MyGame");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, new PlayerData());
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), gameId, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));

        messages.poll(1, TimeUnit.SECONDS);
        String response = messages.poll(1, TimeUnit.SECONDS);

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.GAME_JOINED_SUCCESSFULLY);
    }

    @Test
    void testWebSocketHandlerActionSetReady() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "", playerId, -1);
        player.setReady(true);
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.READY, null, player);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), 1, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);
        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getFromPlayer().isReady()).isTrue();
    }

    @Test
    void testWebSocketHandlerReceiveConnectedPlayers() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        int gameId = GameManager.getInstance().createGame(new PlayerData(null, "User", "ID1", -1), "MyGame");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME);
        Wrapper wrapper = new Wrapper(actionJsonObject.getClass().getSimpleName(), gameId, Request.ACTION, actionJsonObject);
        String msg = gson.toJson(wrapper);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.CONNECTED_PLAYERNAMES, null);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.INFO, infoJsonObject);

        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        InfoJsonObject receivedInfoJsonObject = (InfoJsonObject) WrapperHelper.getInstanceFromJson(response);
        GameInfo gameInfo = receivedInfoJsonObject.getGameInfoList().get(0);
        System.out.println(response);
        Set<PlayerData> players = gameInfo.getConnectedPlayers().stream().filter(p -> p.getId().equals(playerId)).collect(Collectors.toSet());
        assertThat(players).isNotEmpty();
    }
    @Test
    void testWebSocketHandlerActionLeaveLobby() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "200";
        PlayerData playerOne = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, playerOne);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + p_id, p_id, -1);
        int gameId = GameManager.getInstance().createGame(player, "MyGame");
        GameManager.getInstance().getGameById(gameId).joinGame(new PlayerData());

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        player.setGameId(gameId);
        actionJsonObject = new ActionJsonObject(Action.LEAVE_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION , actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.LEAVE_GAME);
        assertThat(GameManager.getInstance().getGameById(gameId).getPlayers().size()).isSameAs(1);
    }
    @Test
    void testWebSocketHandlerActionLeaveGameNoHostLeft() throws Exception {
        WebSocketSession session = initStompSession();

        String  p_id = "300";
        PlayerData playerOne = new PlayerData(null, "Player" + p_id, p_id, -1);
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT, playerOne);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.CONNECT, connectJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + p_id, p_id, -1);


        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);

        int gameId = actionJsonObjectReceived.getFromPlayer().getGameId();

        player.setGameId(gameId);
        actionJsonObject = new ActionJsonObject(Action.LEAVE_GAME, null, player);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION , actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        response = messages.poll(1, TimeUnit.SECONDS);
        actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.LEAVE_GAME);
        assertThat(GameManager.getInstance().getGameById(gameId)).isNull();
    }
    @Test
    void testWebSocketHandlerActionInitFields() throws Exception {
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


        messages.poll(1, TimeUnit.SECONDS);
        messages.poll(1, TimeUnit.SECONDS);
        messages.poll(1, TimeUnit.SECONDS);
        GameManager gm = GameManager.getInstance();
        Game game = gm.getInstance().getGameById(1);

        assertThat(game.getFields().size()).isSameAs(30);

    }

    @Test
    void testWebSocketHandlerActionInitGame() throws Exception {
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
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.GAME_STARTED);
        assertThat(GameManager.getInstance().getGameById(gameId).getFields().size()).isSameAs(fields.size());
    }

    @Test
    void testWebSocketHandlerActionBuyField() throws Exception {
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
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.BUY_FIELD);
        assertThat(game.getFields().get(0).getOwner().getUsername()).isEqualTo(player.getUsername());

    }
    @Test
    void testWebSocketHandlerActionBuyBuilding() throws Exception {
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

        actionJsonObject = new ActionJsonObject(Action.BUY_BUILDING, null, player, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        Game game = GameManager.getInstance().getGameById(gameId);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.BUY_BUILDING);
        assertThat(game.getFields().get(0).getOwner().getUsername()).isEqualTo(player.getUsername());
    }

    @Test
    void testWebSocketHandlerActionSubmitCheat() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID18", -1);

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
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.UPDATE_MONEY);
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
    public void testWebSocketHandlerActionReportCheat() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID19", -1);

        List<Field> fields = List.of(new Field(0, "Field1", true), new Field(1, "Knast", false));
        PlayerData playerData = new PlayerData(null, "U1", "ID1", -1);
        playerData.setHasCheated(true);
        int gameId = GameManager.getInstance().createGame(playerData, "Game200");
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

        actionJsonObject = new ActionJsonObject(Action.SUBMIT_CHEAT, null, playerData, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(2, TimeUnit.SECONDS);
        WrapperHelper.getInstanceFromJson(response);
        ActionJsonObject actionJsonObjectReceived;

        actionJsonObject = new ActionJsonObject(Action.REPORT_CHEAT, playerData.getId(), player, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        response = messages.poll(5, TimeUnit.SECONDS);

        actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assert actionJsonObjectReceived != null;

        assertThat(actionJsonObjectReceived.getAction() == Action.REPORT_CHEAT).isTrue();
        assertThat(playerData.isHasCheated()).isFalse();
        assertThat(playerData.getMoney() == 200).isTrue();
    }
    @Test
    public void testWebSocketHandlerActionReportFalseCheat() throws Exception {
        WebSocketSession session = initStompSession();

        String id = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);


        List<Field> fields = List.of(new Field(0, "Field1", true), new Field(1, "Knast", false));
        PlayerData playerData = new PlayerData(null, "U1", "ID20", -1);
        int gameId = GameManager.getInstance().createGame(playerData, "Game200");

        PlayerData player = new PlayerData(null, "Name1", id, gameId);
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

        actionJsonObject = new ActionJsonObject(Action.REPORT_CHEAT, playerData.getId(), player, fields);
        msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(5, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        assert actionJsonObjectReceived != null;

        assertThat(actionJsonObjectReceived.getAction() == Action.UPDATE_MONEY).isTrue();
        assertThat(actionJsonObjectReceived.getFromPlayer().getMoney() == 200).isTrue();
    }

    @Test
    void testWebSocketHandlerActionBuyFieldNoGameFound() throws Exception {
        WebSocketSession session = initStompSession();

        connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.BUY_FIELD, null, null, List.of(new Field(1, "Field1", true)));
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        String response = messages.poll(1, TimeUnit.SECONDS);

        assertThat(response).isNull();
    }


    @Test
    void testWebSocketHandlerActionMovePlayerNotNull() throws Exception {
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

        assertThat(actionJsonObjectReceived.getParam()).isEqualTo("2");
    }

    @Test
    void testWebSocketHandlerActionEndTurn() throws Exception {
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
        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.END_TURN);
    }


    @Test
    void testWebSocketHandlerActionUpdatePlayerNotNull() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        int gameId = GameManager.getInstance().createGame(player, "game1");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, new PlayerData());
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        player.setMoney(200);
        player.setGameId(gameId);

        actionJsonObject = new ActionJsonObject(Action.UPDATE_MONEY, null, player, null);
        msg = WrapperHelper.toJsonFromObject(gameId,  Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        System.out.println(response);

       assertThat(actionJsonObjectReceived.getFromPlayer().getMoney()).isEqualTo(200);
    }


    @Test
    void testWebSocketHandlerActionUpdatePlayerNull() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        int gameId = GameManager.getInstance().createGame(player, "game1");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, new PlayerData());
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        player.setMoney(200);
        player.setGameId(gameId);

        actionJsonObject = new ActionJsonObject(Action.UPDATE_MONEY, null, new PlayerData(null, "", "", gameId), null);
        msg = WrapperHelper.toJsonFromObject(gameId,  Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        assertThat(response).isNull();
    }
    @Test
    void testWebSocketHandlerActionPayTaxes() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        int gameId = GameManager.getInstance().createGame(player, "game1");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, new PlayerData());
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        PlayerData player1 = new PlayerData();
        player1.copyFrom(player);
        player1.setMoney(200);
        player1.setGameId(gameId);

        actionJsonObject = new ActionJsonObject(Action.PAY_TAXES, null, player1, null);
        msg = WrapperHelper.toJsonFromObject(gameId,  Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);
        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response);
        System.out.println(response);

        assertThat(actionJsonObjectReceived.getFromPlayer().getMoney()).isEqualTo(200);
        assertThat(Integer.parseInt(actionJsonObjectReceived.getParam())).isEqualTo(1300);
    }

    @Test
    void testWebSocketHandlerActionPlayerActionPlayerIsNull() throws Exception {
        WebSocketSession session = initStompSession();

        String username = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, username, "ID1", -1);

        int gameId = GameManager.getInstance().createGame(player, "game1");

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.JOIN_GAME, null, new PlayerData());
        String msg = WrapperHelper.toJsonFromObject(gameId, Request.ACTION, actionJsonObject);

        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);
        player.setMoney(200);
        player.setGameId(gameId);

        actionJsonObject = new ActionJsonObject(Action.PAY_TAXES, null, new PlayerData(null, "", "", gameId), null);
        msg = WrapperHelper.toJsonFromObject(gameId,  Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        assertThat(response).isNull();
    }

    @Test
    void testWebSocketHandlerActionLeaveGameNotEmpty() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "", playerId, -1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(messages.poll(1, TimeUnit.SECONDS));
        int gameId = actionJsonObjectReceived.getFromPlayer().getGameId();
        player.setGameId(gameId);

        Game game = GameManager.getInstance().getGameById(gameId);
        game.setStarted(true);

        PlayerData player2 = new PlayerData(null, "P2", "ID2",gameId);
        GameManager.getInstance().joinGame(gameId, player2);
        GameManager.getInstance().getGameById(gameId).getPlayers().get(0).setOnTurn(true);


        actionJsonObject = new ActionJsonObject(Action.CONNECTION_LOST, null, player);
        msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);


        PlayerData serverPlayer = GameManager.getInstance().getPlayers(gameId).stream()
                                                            .filter(p -> p.getId().equals("ID2"))
                                                            .findAny().orElse(null);
        assertThat(serverPlayer.isHost()).isTrue();
    }

    @Test
    void testWebSocketHandlerActionLeaveGameEmpty() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "", playerId, -1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(messages.poll(1, TimeUnit.SECONDS));
        int gameId = actionJsonObjectReceived.getFromPlayer().getGameId();
        player.setGameId(gameId);

        Game game = GameManager.getInstance().getGameById(gameId);
        game.setStarted(true);

        actionJsonObject = new ActionJsonObject(Action.CONNECTION_LOST, null, player);
        msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        messages.poll(1, TimeUnit.SECONDS);

        assertThat(GameManager.getInstance().getGameById(gameId)).isNull();
    }

    @Test
    void testWebSocketHandlerActionLeaveGamePlayerNull() throws Exception {
        WebSocketSession session = initStompSession();

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CONNECTION_LOST, null, null);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        assertThat(response).isNull();
    }

    @Test
    void testWebSocketHandlerActionRejoinPlayer() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "", playerId, -1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(messages.poll(1, TimeUnit.SECONDS));
        int gameId = actionJsonObjectReceived.getFromPlayer().getGameId();
        player.setGameId(gameId);

        actionJsonObject = new ActionJsonObject(Action.RECONNECT_OK, null, player);
        msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(messages.poll(1, TimeUnit.SECONDS));

        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.RECONNECT_OK);
    }

    @Test
    void testWebSocketHandlerActionRejoinPlayerNull() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "", playerId, -1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.RECONNECT_OK, null, player);
        String msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        assertThat(response).isNull();
    }

    @Test
    void testWebSocketHandlerActionDiscardReconnect() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + (id-1), playerId, -1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(messages.poll(1, TimeUnit.SECONDS));
        int gameId = actionJsonObjectReceived.getFromPlayer().getGameId();
        player.setGameId(gameId);

        actionJsonObject = new ActionJsonObject(Action.RECONNECT_DISCARD, Integer.toString(gameId), player);
        msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(messages.poll(1, TimeUnit.SECONDS));

        assertThat(actionJsonObjectReceived.getAction()).isSameAs(Action.RECONNECT_DISCARD);
    }

    @Test
    void testWebSocketHandlerActionDiscardReconnectNull() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + (id-1), playerId, -1);

        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME, null, player);
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(messages.poll(1, TimeUnit.SECONDS));
        int gameId = actionJsonObjectReceived.getFromPlayer().getGameId();
        player.setGameId(gameId);

        actionJsonObject = new ActionJsonObject(Action.RECONNECT_DISCARD, Integer.toString(gameId), new PlayerData());
        msg = WrapperHelper.toJsonFromObject(player.getGameId(), Request.ACTION, actionJsonObject);
        session.sendMessage(new TextMessage(msg));
        String response = messages.poll(1, TimeUnit.SECONDS);

        assertThat(response).isNull();
    }

    @Test
    void testAfterConnectionClosedPlayerNoGame() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        session.close();
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = WebSocketHandlerImpl.getInstance().getPlayerByPlayerId(playerId);
        assertThat(player.isConnected()).isFalse();
    }

    @Test
    void testAfterConnectionClosedPlayerNull() throws Exception {
        WebSocketSession session = initStompSession();

        int oldSize = WebSocketHandlerImpl.getInstance().getPlayerIds().size();

        session.close();
        messages.poll(1, TimeUnit.SECONDS);

        int newSize = WebSocketHandlerImpl.getInstance().getPlayerIds().size();
        assertThat(newSize).isEqualTo(oldSize);
    }

    @Test
    void testAfterConnectionClosedGameNotStarted() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + (id-1), playerId, -1);
        int gameId = GameManager.getInstance().createGame(player, "G1");
        WebSocketHandlerImpl.getInstance().getPlayerByPlayerId(playerId).setGameId(gameId);

        session.close();
        messages.poll(1, TimeUnit.SECONDS);

        Game game = GameManager.getInstance().getGameById(gameId);
        assertThat(game).isNull();
    }

    @Test
    void testAfterConnectionClosedGameStarted() throws Exception {
        WebSocketSession session = initStompSession();

        String playerId = connectToWebsocket(session, -1);
        messages.poll(1, TimeUnit.SECONDS);

        PlayerData player = new PlayerData(null, "Player" + (id-1), playerId, -1);
        int gameId = GameManager.getInstance().createGame(player, "G1");
        WebSocketHandlerImpl.getInstance().getPlayerByPlayerId(playerId).setGameId(gameId);
        GameManager.getInstance().getGameById(gameId).setStarted(true);


        session.close();
        messages.poll(1, TimeUnit.SECONDS);

        List<PlayerData> playerServer = GameManager.getInstance().getPlayers(gameId);
        assertThat(playerServer).hasSize(1);
    }

    @Test
    void testWebSocketHandlerActionRISIKO_CARD_SHOW() throws Exception {
        WebSocketSession session = initStompSession(); //enable connection
        connectToWebsocket(session, -1); // make connection to lokal test instanz of server

        messages.poll(1, TimeUnit.SECONDS); //needed to get initial connectJsonObject
        //initialize client results
        int cardIndex = 2;
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.RISIKO_CARD_SHOW, Integer.toString(cardIndex), null); // Action, card, and player send to server
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject); //create message json object of previous object with new parameters

        session.sendMessage(new TextMessage(msg)); //sends message to the server
        String response = messages.poll(1, TimeUnit.SECONDS); //response of the server

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response); //pass json from server response
        int responseCardIndex = gson.fromJson(actionJsonObjectReceived.getParam(), int.class); //makes params into a new int[] with size 2 -> first argument: String with json format, second argument: desired class
        messages.clear(); //empty message

        assertThat(cardIndex == responseCardIndex);
    }

    @Test
    void testWebSocketHandlerActionBANK_CARD_SHOW() throws Exception {
        WebSocketSession session = initStompSession(); //enable connection
        connectToWebsocket(session, -1); // make connection to lokal test instanz of server
        messages.poll(1, TimeUnit.SECONDS); //needed to get initial connectJsonObject
        //initialize client results
        int cardIndex = 10;
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.BANK_CARD_SHOW, Integer.toString(cardIndex), null); // Action, card, and player send to server
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject); //create message json object of previous object with new parameters

        session.sendMessage(new TextMessage(msg)); //sends message to the server
        String response = messages.poll(1, TimeUnit.SECONDS); //response of the server

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response); //pass json from server response
        int responseCardIndex = gson.fromJson(actionJsonObjectReceived.getParam(), int.class); //makes params into a new int[] with size 2 -> first argument: String with json format, second argument: desired class
        messages.clear(); //empty message

        assertThat(cardIndex == responseCardIndex);
    }
    @Test
    void testWebSocketHandlerActionBANK_CARD_SHOWwithMinusIndex() throws Exception {
        WebSocketSession session = initStompSession(); //enable connection
        connectToWebsocket(session, -1); // make connection to lokal test instanz of server
        messages.poll(1, TimeUnit.SECONDS); //needed to get initial connectJsonObject
        //initialize client results
        int cardIndex = -10;
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.BANK_CARD_SHOW, Integer.toString(cardIndex), null); // Action, card, and player send to server
        String msg = WrapperHelper.toJsonFromObject(-1, Request.ACTION, actionJsonObject); //create message json object of previous object with new parameters

        session.sendMessage(new TextMessage(msg)); //sends message to the server
        String response = messages.poll(1, TimeUnit.SECONDS); //response of the server

        ActionJsonObject actionJsonObjectReceived = (ActionJsonObject) WrapperHelper.getInstanceFromJson(response); //pass json from server response
        int responseCardIndex = gson.fromJson(actionJsonObjectReceived.getParam(), int.class); //makes params into a new int[] with size 2 -> first argument: String with json format, second argument: desired class
        messages.clear(); //empty message

        assertThat(cardIndex == responseCardIndex);
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
}
