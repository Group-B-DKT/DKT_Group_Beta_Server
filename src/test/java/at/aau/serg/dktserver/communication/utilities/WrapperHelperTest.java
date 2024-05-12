package at.aau.serg.dktserver.communication.utilities;

import at.aau.serg.dktserver.communication.enums.Action;
import at.aau.serg.dktserver.communication.enums.ConnectType;
import at.aau.serg.dktserver.communication.enums.Info;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.ConnectJsonObject;
import at.aau.serg.dktserver.communication.InfoJsonObject;
import at.aau.serg.dktserver.communication.Wrapper;

import static at.aau.serg.dktserver.communication.enums.Request.CONNECT;
import static at.aau.serg.dktserver.communication.enums.Request.ACTION;
import static at.aau.serg.dktserver.communication.enums.Request.INFO;

public class WrapperHelperTest {

    @Test
    public void testGetInstanceFromWrapper_ConnectJsonObject() {
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.CONNECTION_ESTABLISHED);
        Wrapper wrapper = new Wrapper("ConnectJsonObject", 1, CONNECT, connectJsonObject);
        assertEquals(connectJsonObject.getClass(), WrapperHelper.getInstanceFromWrapper(wrapper).getClass());
    }

    @Test
    public void testGetInstanceFromWrapper_ActionJsonObject() {
        ActionJsonObject actionJsonObject = new ActionJsonObject(Action.CREATE_GAME);
        Wrapper wrapper = new Wrapper("ActionJsonObject", 1, ACTION, actionJsonObject);
        assertEquals(actionJsonObject.getClass(), WrapperHelper.getInstanceFromWrapper(wrapper).getClass());
    }

    @Test
    public void testGetInstanceFromWrapper_InfoJsonObject() {
        InfoJsonObject infoJsonObject = new InfoJsonObject(Info.GAME_LIST, null);
        Wrapper wrapper = new Wrapper("InfoJsonObject", 1, INFO, infoJsonObject);
        assertEquals(infoJsonObject.getClass(), WrapperHelper.getInstanceFromWrapper(wrapper).getClass());
    }

    @Test
    public void testGetInstanceFromJson_NullInput() {
        assertNull(WrapperHelper.getInstanceFromJson(null));
    }

    @Test
    public void testToJsonFromObject() {
        ConnectJsonObject connectJsonObject = new ConnectJsonObject(ConnectType.NEW_CONNECT);
        String json = WrapperHelper.toJsonFromObject(1, CONNECT, connectJsonObject);
        assertNotNull(json);
        assertTrue(json.contains("ConnectJsonObject"));
    }
}
