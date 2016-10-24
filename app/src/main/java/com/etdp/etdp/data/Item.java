package com.etdp.etdp.data;

import org.json.JSONObject;

/**
 * Created by test on 10/24/2016.
 */

public class Item implements JSONPopulator {
    private Condition condition;

    public Condition getCondition() {
        return condition;
    }

    @Override
    public void populate(JSONObject object) {
        condition = new Condition();
        condition.populate(object.optJSONObject("condition"));
    }
}
