package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.CardType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardTest {
    @Test
    public void testJokerCard(){
        JokerCard jokerCard = new JokerCard(2,0, CardType.JOKER,"xyz");
        assertEquals(jokerCard.getAmount(), 0);
    }
}
