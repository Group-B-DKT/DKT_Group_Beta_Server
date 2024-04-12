package at.aau.serg.dktserver.model.domain.fields;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;

public class Start extends Field {
    public Start(int id, String name, int position) {
        super(id, name, position);
    }

    @Override
    public void enterField(PlayerData playerData) {

    }
}
