package at.aau.serg.dktserver.model.interfaces;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;

import java.util.ArrayList;

public interface GameHandler {
    int roll_dice();
    int roll_dice(PlayerData playerData);
    void start(PlayerData player);
    void setOrder();

    void joinGame(PlayerData player);
    boolean buyField(int fieldId, PlayerData playerData);

    void setFields(ArrayList<Field> fields);
}
