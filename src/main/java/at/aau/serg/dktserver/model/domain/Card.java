package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.CardType;

import java.io.Serializable;

public class Card implements Serializable {
    private int id;
    private int amount;
    private String imageResource;
    private CardType type;

    public Card(int id, int amount, CardType type,String imageResource) {
        this.id = id;
        this.amount = amount;
        this.imageResource = imageResource;
        this.type = type;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public String getImageResource() {
        return imageResource;
    }
}

