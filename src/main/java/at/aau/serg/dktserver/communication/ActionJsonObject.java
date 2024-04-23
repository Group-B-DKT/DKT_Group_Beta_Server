package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.model.domain.PlayerData;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ActionJsonObject {
    private Action action;
    private String param;
    private PlayerData fromPlayer;

    public ActionJsonObject(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }
    public String getParam() {
        return param;
    }

    public PlayerData getFromPlayer(){return fromPlayer;}

}
