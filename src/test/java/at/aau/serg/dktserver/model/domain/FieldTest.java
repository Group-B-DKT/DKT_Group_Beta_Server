package at.aau.serg.dktserver.model.domain;

import at.aau.serg.dktserver.model.enums.FieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {
    PlayerData playerData;
    @Mock
    WebSocketSession socketSession;
    @BeforeEach
    void setUp() {
        playerData = new PlayerData(socketSession, "Player 1", "1", 1);
    }

    @Test
    void getName() {
        Field f1 = new Field(1, "Example 1", 100, true, FieldType.NORMAL);
        assertEquals("Example 1", f1.getName());
    }

    @Test
    void enterField() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertEquals(playerData.getId(), f1.enterField(playerData));
    }

    @Test
    void getId() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertEquals(1, f1.getId());
    }

    @Test
    void getPrice() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertEquals(100, f1.getPrice());
    }

    @Test
    void getOwner() {
        Field f1 = new Field(1, "Example 1", 100, true);
        f1.setOwner(playerData);
        assertEquals(playerData, f1.getOwner());
    }

    @Test
    void getOwnable() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertTrue(f1.getOwnable());
    }
    @Test
    void getOwnableFalse() {
        Field f1 = new Field(1, "Start", false);
        assertFalse(f1.getOwnable());
    }

    @Test
    void testEqualsTrue() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(1, "Example 1", 100, true);
        assertEquals(f1, f2);
    }
    @Test
    void testEqualsTrue2() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(1, "Example 1", 100, true);
        f1.setOwner(playerData);
        f2.setOwner(playerData);
        assertEquals(f1, f2);
    }
    @Test
    void testEqualsFalse() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(2, "Example 2", 150, true);
        assertNotEquals(f1, f2);
    }
    @Test
    void testEqualsFalse2() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(2, "Example 2",  false);
        assertNotEquals(f1, f2);
    }
    @Test
    void testEqualsFalse3() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(2, "Example 2", 150, true);
        f1.setOwner(playerData);
        f2.setOwner(new PlayerData());
        assertNotEquals(f1, f2);
    }
    @Test
    void testEqualsFalse4() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(2, "Example 2", 150, true);
        f1.setOwner(playerData);
        f2.setOwner(playerData);
        assertNotEquals(f1, f2);
    }

    @Test
    void testEqualsNull() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertNotEquals(f1, null);
    }

    @Test
    void testEqualsWrongObject() {
        Field f1 = new Field(1, "Example 1", 100, true);
        assertNotEquals(f1, playerData);
    }
    @Test
    void testEqualsDifferentNames() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(1, "Example 2", 100, true);
        assertNotEquals(f1, f2);
    }
    @Test
    void testEqualsDifferentOwners() {
        Field f1 = new Field(1, "Example 1", 100, true);
        f1.setOwner(playerData);
        Field f2 = new Field(1, "Example 1", 100, true);
        f2.setOwner(new PlayerData(socketSession, "Player 2", "2", 1));
        assertNotEquals(f1, f2);
    }
    @Test
    void testEqualsDifferentOwnable() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(1, "Example 1", 100, false);
        assertNotEquals(f1, f2);
    }

    @Test
    void testHashCode() {
        Field f1 = new Field(1, "Example 1", 100, true);
        Field f2 = new Field(1, "Example 1", 100, true);
        assertEquals(f1.hashCode(), f2.hashCode());
    }
    @Test
    void testGetHotel() {
        Field field = new Field(1, "Example 1", 100, true);
        Hotel hotel = new Hotel(500, 1);
        field.setHotel(hotel);
        assertEquals(hotel, field.getHotel());
    }

    @Test
    void testSetHotel() {
        Field field = new Field(1, "Example 1", 100, true);
        Hotel hotel = new Hotel(400, 1);
        field.setHotel(hotel);
        assertEquals(hotel, field.getHotel());
    }

    @Test
    void testGetHouses() {
        Field field = new Field(1, "Example 1", 100, true);
        House house = new House(400,1);
        field.addHouse(house);
        assertTrue(field.getHouses().contains(house));
    }

    @Test
    void testAddHouse() {
        Field field = new Field(1, "Example 1", 100, true);
        House house = new House(400, 1);
        field.addHouse(house);
        assertTrue(field.getHouses().contains(house));
    }
}