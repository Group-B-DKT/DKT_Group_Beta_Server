package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.FieldType;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

public class Field {

    @Getter
    private int id;
    private String name;
    private int price = 0;
    private PlayerData owner;
    private final boolean ownable;

    private FieldType fieldType;
    private int rent;

    public Field(int id, String name, int price, boolean ownable, FieldType fieldType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ownable = ownable;
        this.fieldType = fieldType;
    }


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

    public int getRent() {
        return rent;
    }
    public void setRent(int rent) {
        this.rent = rent;
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
