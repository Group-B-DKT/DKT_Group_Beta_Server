package at.aau.serg.dktserver.parser.interfaces;

import org.springframework.web.socket.WebSocketSession;

public interface InputParser {
    void parseInput(String client_msg, WebSocketSession session, String fromPlayername);
}
