package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.interfaces.GameHandler;
import lombok.Getter;

import java.security.SecureRandom;

public class Game implements GameHandler {
    private SecureRandom rng;

    @Getter
    private int id;

    public Game(int id) {

        this.id = id;
        rng = new SecureRandom();
    }


    @Override
    public int roll_dice(){
        return rng.nextInt(6)+1;
    }

}
