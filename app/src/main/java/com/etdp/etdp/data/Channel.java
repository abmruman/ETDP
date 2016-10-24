package com.etdp.etdp.data;

import org.json.JSONObject;

/**
 * Created by test on 10/24/2016.
 */

public class Channel implements JSONPopulator {
    private Units units;
    private Item item;

    public Units getUnits() {
        return units;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public void populate(JSONObject object) {
        units = new Units();
        units.populate(object.optJSONObject("units"));

        item = new Item();
        item.populate(object.optJSONObject("item"));
    }
}
