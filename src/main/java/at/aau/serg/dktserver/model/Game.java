package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.interfaces.GameHandler;
import lombok.Getter;

public class Game implements GameHandler {
    @Getter
    private int id;

    public Game(int id) {
        this.id = id;
    }


    @Override
    public int roll_dice(){
        return (int) (Math.random() * 10) % 6 + 1;
    }

}
