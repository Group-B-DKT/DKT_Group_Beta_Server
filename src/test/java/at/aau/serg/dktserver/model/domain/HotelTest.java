package at.aau.serg.dktserver.model.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HotelTest {


    @Test
    void testGetPrice() {
        Hotel hotel = new Hotel(400, 1);
        assertEquals(400, hotel.getPrice());
    }

    @Test
    void testGetPosition() {
        Hotel hotel = new Hotel(400, 1);
        assertEquals(1, hotel.getPosition());
    }

    @Test
    void testGetHotelPrice() {
        assertEquals(400, Hotel.getHotelPrice());
    }
}