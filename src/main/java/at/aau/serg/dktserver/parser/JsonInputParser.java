package at.aau.serg.dktserver.parser;

import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.parser.interfaces.InputParser;
import com.google.gson.Gson;
import org.springframework.web.socket.WebSocketSession;

public class JsonInputParser implements InputParser {
    private static Gson gson = new Gson();

    @Override
    public void parseInput(String client_msg, WebSocketSession session, String fromPlayername) {

    }


    private void parseConnect(Wrapper wrapper, WebSocketSession session){

    }
}
