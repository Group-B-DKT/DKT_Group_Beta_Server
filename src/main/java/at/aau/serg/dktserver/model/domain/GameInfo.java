package at.aau.serg.dktserver.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class GameInfo {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private int connectedPlayer;
}
