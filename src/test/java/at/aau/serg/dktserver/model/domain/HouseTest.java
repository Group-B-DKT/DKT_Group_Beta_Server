package at.aau.serg.dktserver.model.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HouseTest {
    @Test
    void testGetHousePrice() {
        assertEquals(200, House.getHousePrice());
    }

    @Test
    void testGetMaxAmount() {
        House house = new House(200, 1);
        assertEquals(4, house.getMaxAmount());
    }
}