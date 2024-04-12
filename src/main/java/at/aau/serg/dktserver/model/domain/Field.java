package at.aau.serg.dktserver.model.domain;

import lombok.Getter;

public abstract class Field {

    @Getter
    private int id;
    private String name;
    private int position;

    public Field(int id, String name, int position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
    public abstract void enterField(PlayerData playerData);

    public int getId() {
        return id;
    }
}
