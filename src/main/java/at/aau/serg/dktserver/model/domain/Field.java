package at.aau.serg.dktserver.model.domain;

import lombok.Getter;

public class Field {

    @Getter
    private int id;
    private String name;
    private int position;
    private int price;
    private PlayerData owner;

    public Field(int id, String name, int position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }
    public Field(int id, String name, int position, int price) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
    public void enterField(PlayerData playerData) {
        return;
    }

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public void setOwner(PlayerData playerData) {
    }
}
