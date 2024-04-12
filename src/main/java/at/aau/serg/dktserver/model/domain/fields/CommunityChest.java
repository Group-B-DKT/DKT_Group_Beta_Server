package at.aau.serg.dktserver.model.domain.fields;

import at.aau.serg.dktserver.model.domain.Field;
import at.aau.serg.dktserver.model.domain.PlayerData;

public class CommunityChest extends Field {
    public CommunityChest(int id, String name, int position) {
        super(id, name, position);
    }

    @Override
    public void enterField(PlayerData playerData) {

    }
}
