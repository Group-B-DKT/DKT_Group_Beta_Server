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
}
