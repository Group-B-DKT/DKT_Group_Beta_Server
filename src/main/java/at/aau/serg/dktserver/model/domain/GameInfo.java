package at.aau.serg.dktserver.model.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GameInfo {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private int connectedPlayer;


}
