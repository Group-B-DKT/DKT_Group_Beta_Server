package at.aau.serg.dktserver.parser;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.ConnectJsonObject;
import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.communication.enums.ConnectType;
import at.aau.serg.dktserver.communication.utilities.WrapperHelper;
import at.aau.serg.dktserver.controller.ActionController;
import at.aau.serg.dktserver.controller.ConnectController;
import at.aau.serg.dktserver.parser.interfaces.InputParser;
import com.google.gson.Gson;
import org.springframework.web.socket.WebSocketSession;

public class JsonInputParser implements InputParser {
    private static Gson gson = new Gson();
    private ConnectController connectController;
    private ActionController actionController;

    public JsonInputParser(){
        actionController = new ActionController();
        connectController = new ConnectController();
    }

    @Override
    public void parseInput(String client_msg, WebSocketSession session, String fromPlayername) {
        Wrapper wrapper;
        try {
            wrapper = gson.fromJson(client_msg, Wrapper.class);
        }catch (Exception e){
            return;
        }
        switch (wrapper.getRequest()){
            case CONNECT -> parseConnect(wrapper, session);
            case ACTION -> parseAction(wrapper, fromPlayername);
        }
    }


    private void parseConnect(Wrapper wrapper, WebSocketSession session){
        Object jsonObject = WrapperHelper.getInstanceFromWrapper(wrapper);
        ConnectJsonObject connectJsonObject = jsonObject instanceof ConnectJsonObject ? (ConnectJsonObject) jsonObject : null;

        if (connectJsonObject == null) return;
        switch (connectJsonObject.getConnectType()){
            case NEW_CONNECT -> connectController.connectUser(connectJsonObject.getUsername(), connectJsonObject.getPlayerId(), wrapper.getGameId(), session);
        }
    }

    private void parseAction(Wrapper wrapper, String fromPLayername){
        Object jsonObject = WrapperHelper.getInstanceFromWrapper(wrapper);
        ActionJsonObject actionJsonObject = jsonObject instanceof ActionJsonObject ? (ActionJsonObject) jsonObject : null;

        if (actionJsonObject == null) return;
        System.out.println(actionJsonObject);
        actionController.callAction(actionJsonObject.getAction(), wrapper.getGameId(), fromPLayername);
    }
}
