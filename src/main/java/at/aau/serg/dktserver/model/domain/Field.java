package at.aau.serg.dktserver.model.domain;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString
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
        return playerData.getId();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return id == field.id && price == field.price && ownable == field.ownable && Objects.equals(name, field.name) && Objects.equals(owner, field.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, owner, ownable);
    }
}
