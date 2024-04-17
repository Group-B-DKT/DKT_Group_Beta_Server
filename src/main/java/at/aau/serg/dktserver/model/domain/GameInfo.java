package at.aau.serg.dktserver.model.domain;

import com.google.gson.annotations.Expose;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
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
