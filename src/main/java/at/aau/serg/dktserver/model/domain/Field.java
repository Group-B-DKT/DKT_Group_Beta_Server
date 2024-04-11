package at.aau.serg.dktserver.model.domain;

public abstract class Field {
    private String name;
    private int position;

    public Field(String name, int position) {
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
}
