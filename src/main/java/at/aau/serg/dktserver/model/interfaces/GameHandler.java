package at.aau.serg.dktserver.model.interfaces;

import at.aau.serg.dktserver.model.domain.PlayerData;

public interface GameHandler {
    int roll_dice();
    void start(PlayerData player);
    void setOrder();

    void joinGame(PlayerData player);
    void removePlayer(PlayerData player);
}
