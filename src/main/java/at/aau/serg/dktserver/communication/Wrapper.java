package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Wrapper {
    @Getter
    private String classname;
    @Getter
    private int gameId;
    @Getter
    private Request request;
    @Getter
    private Object object;
}
