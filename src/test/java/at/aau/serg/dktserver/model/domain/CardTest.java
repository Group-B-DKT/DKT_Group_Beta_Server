package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardTest {

    private Card card;

    @BeforeEach
    public void setUp() {
        // Initialize a Card instance before each test
        card = new Card(1, 100, CardType.PAY, "image.png");
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(1, card.getId());
        assertEquals(100, card.getAmount());
        assertEquals(CardType.PAY, card.getType());
        assertEquals("image.png", card.getImageResource());
    }

    @Test
    public void testSetters() {
        card.setId(2);
        assertEquals(2, card.getId());

        card.setAmount(200);
        assertEquals(200, card.getAmount());

        card.setType(CardType.MOVE);
        assertEquals(CardType.MOVE, card.getType());

        card.setImageResource("new_image.png");
        assertEquals("new_image.png", card.getImageResource());
    }
    @Test
    public void testJokerCard(){
        JokerCard jokerCard = new JokerCard(2,0, CardType.JOKER,"xyz");
        assertEquals(jokerCard.getAmount(), 0);
    }
}
