package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.Action;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ActionJsonObject {
    private Action action;
    private String param;
    private String fromPlayername;
    
    public Action getAction() {
        return action;
    }
    public String getParam() {
        return param;
    }

    public String getFromPlayername(){return fromPlayername;}

}
