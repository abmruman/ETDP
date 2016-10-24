package com.etdp.etdp.data;

import org.json.JSONObject;

/**
 * Created by test on 10/24/2016.
 */

public class Units implements JSONPopulator {
    private String temperature;

    public String getTemperature() {
        return temperature;
    }

    @Override
    public void populate(JSONObject object) {
        temperature = object.optString("temperature");
    }
}
