package at.aau.serg.dktserver.model.domain;

import lombok.Getter;

public class Field {

    @Getter
    private int id;
    private String name;
    private int price = 0;
    private PlayerData owner;
    private final boolean ownable;

    public Field(int id, String name, boolean ownable) {
        this.id = id;
        this.name = name;
        this.ownable = ownable;
    }
    public Field(int id, String name, int price, boolean ownable) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ownable = ownable;
    }

    public String getName() {
        return name;
    }

    public String enterField(PlayerData playerData) {
        return playerData.getPlayerId();
    }

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public void setOwner(PlayerData playerData) {
        owner = playerData;
    }

    public boolean getOwnable() {
        return ownable;
    }

    public PlayerData getOwner() {
        return owner;
    }
}
