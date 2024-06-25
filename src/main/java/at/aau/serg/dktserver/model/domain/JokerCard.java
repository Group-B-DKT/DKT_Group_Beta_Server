package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.CardType;

import java.io.Serializable;

public class JokerCard extends Card implements Serializable {
    public JokerCard(int id, int amount, CardType type, String imageResource) {
        super(id, amount, type, imageResource);
    }
}