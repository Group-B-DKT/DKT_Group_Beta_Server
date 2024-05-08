package at.aau.serg.dktserver.model.domain;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode
public class GameInfo {
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private List<PlayerData> connectedPlayers;

    @Getter
    @Setter
    boolean isStarted;

    public GameInfo(int id, String name, List<PlayerData> connectedPlayers) {
        this.id = id;
        this.name = name;
        this.connectedPlayers = connectedPlayers;
        this.isStarted = false;
    }
}
