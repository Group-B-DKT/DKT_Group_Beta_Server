package at.aau.serg.dktserver.model.domain.fields;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;

public class FreeParking extends Field {
    public FreeParking(String name, int position) {
        super(name, position);
    }

    @Override
    public void enterField(PlayerData playerData) {

    }
}
