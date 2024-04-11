package at.aau.serg.dktserver.model.domain.fields;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;

public class GoToPrison extends Field {
    public GoToPrison(String name, int position) {
        super(name, position);
    }

    @Override
    public void enterField(PlayerData playerData) {

    }
}
