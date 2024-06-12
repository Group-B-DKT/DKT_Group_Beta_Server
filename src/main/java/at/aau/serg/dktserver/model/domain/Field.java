package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.FieldType;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Field {

    @Getter
    private int id;
    private String name;
    private int price = 0;
    private PlayerData owner;
    private final boolean ownable;

    private transient List<Building> buildings;

    private FieldType fieldType;

    public Field(int id, String name, int price, boolean ownable, FieldType fieldType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ownable = ownable;
        this.fieldType = fieldType;
        this.buildings = new ArrayList<>();
    }


    public Field(int id, String name, boolean ownable) {
        this(id, name, -1,ownable, FieldType.NORMAL);
    }
    public Field(int id, String name, int price, boolean ownable) {
        this(id, name, price, ownable, FieldType.NORMAL);
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

    public List<Building> getBuildings(){
        return this.buildings;
    }

    public void addBuilding(Building building){
        buildings.add(building);
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
