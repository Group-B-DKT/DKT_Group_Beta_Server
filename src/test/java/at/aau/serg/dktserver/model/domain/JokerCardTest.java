package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JokerCardTest {

    private JokerCard jokerCard;

    @BeforeEach
    public void setUp() {
        jokerCard = new JokerCard(1, 0, CardType.JOKER, "joker.png");
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(1, jokerCard.getId());
        assertEquals(0, jokerCard.getAmount());
        assertEquals(CardType.JOKER, jokerCard.getType());
        assertEquals("joker.png", jokerCard.getImageResource());
    }

    @Test
    public void testInheritedMethods() {
        jokerCard.setId(3);
        jokerCard.setAmount(100);
        jokerCard.setType(CardType.PAY);
        jokerCard.setImageResource("new_joker.png");

        assertEquals(3, jokerCard.getId());
        assertEquals(100, jokerCard.getAmount());
        assertEquals(CardType.PAY, jokerCard.getType());
        assertEquals("new_joker.png", jokerCard.getImageResource());
    }
}
