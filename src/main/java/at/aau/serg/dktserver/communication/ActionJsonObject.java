package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@ToString
public class ActionJsonObject {
    @Getter
    private Action action;
    @Getter
    private String param;
    @Getter
    private PlayerData fromPlayer;
    @Getter
    private List<Field> fields;

    public ActionJsonObject(Action action, String param, PlayerData fromPlayer) {
        this.action = action;
        this.param = param;
        this.fromPlayer = fromPlayer;
    }

    public ActionJsonObject(Action action) {
        this.action = action;
    }


}
