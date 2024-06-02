package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.ConnectType;
import at.aau.serg.dktserver.model.domain.GameInfo;
import at.aau.serg.dktserver.model.domain.PlayerData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ConnectJsonObject {
    @Getter
    private ConnectType connectType;

    @Getter
    private PlayerData player;

    @Getter
    private GameInfo gameInfo;

    public ConnectJsonObject(ConnectType connectType, PlayerData player){
        this(connectType, player, null);
    }
    public ConnectJsonObject(ConnectType connectType) {
        this(connectType, null, null);
    }
    public ConnectJsonObject(ConnectType connectType, GameInfo gameInfo) {
        this(connectType, null, gameInfo);
    }

}
