package at.aau.serg.dktserver.communication;

import at.aau.serg.dktserver.communication.enums.Info;
import at.aau.serg.dktserver.model.domain.GameInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@ToString
public class InfoJsonObject {
    @Getter
    private Info info;
    @Getter
    private List<GameInfo> gameInfoList;
}
