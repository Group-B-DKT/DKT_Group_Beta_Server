package at.aau.serg.dktserver.model.interfaces;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;

import java.util.ArrayList;

public interface GameHandler {
    int roll_dice();

    void start(PlayerData player);

    void setOrder();

    void joinGame(PlayerData player);

    boolean buyField(int fieldId, PlayerData playerData);

    void setFields(ArrayList<Field> fields);

    PlayerData removePlayerAndChangeHost(PlayerData player);

    void updateField(Field field);

    int getFreePlayerColor();

    boolean setPlayerPosition(String playerId, int amount);

    PlayerData getNextPlayer(PlayerData playerByUsername);

    boolean updatePlayer(PlayerData player);

    void goToPrison(PlayerData player);
  
    PlayerData getNewHost();

    boolean removePlayer(PlayerData player);

    void removeFieldOwner(String id);

    boolean isOnTurn(String fromPlayerId);

    boolean checkWinCondition();
}
