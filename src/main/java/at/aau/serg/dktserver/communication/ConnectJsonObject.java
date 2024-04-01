package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.ConnectType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ConnectJsonObject {
    @Getter
    private ConnectType connectType;
    @Getter
    private String playerId;
    @Getter
    private String username;

    public ConnectJsonObject(ConnectType connectType) {
        this.connectType = connectType;
    }
}
