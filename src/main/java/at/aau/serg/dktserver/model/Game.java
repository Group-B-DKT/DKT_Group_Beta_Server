package at.aau.serg.dktserver.model;

import at.aau.serg.dktserver.model.interfaces.GameHandler;
import lombok.Getter;

import java.util.Random;

public class Game implements GameHandler {
    private Random rng;

    @Getter
    private int id;

    public Game(int id) {

        this.id = id;
        rng = new Random();
    }


    @Override
    public int roll_dice(){
        return rng.nextInt(6)+1;
    }

}
