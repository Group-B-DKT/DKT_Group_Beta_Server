package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.communication.enums.Info;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@AllArgsConstructor
@ToString
public class InfoJsonObject {
    @Getter
    private Info info;
    @Getter
    private int gameId;
    @Getter
    private Map<Integer, Integer> gameInfo;
}
