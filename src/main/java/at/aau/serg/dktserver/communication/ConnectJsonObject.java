package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.ConnectType;
import at.aau.serg.dktserver.model.domain.PlayerData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ConnectJsonObject {
    @Getter
    private ConnectType connectType;

    @Getter
    private PlayerData player;

    public ConnectJsonObject(ConnectType connectType) {
        this.connectType = connectType;
    }

}
