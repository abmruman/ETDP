package com.etdp.etdp.service;

import com.etdp.etdp.data.Channel;

/**
 * Created by test on 10/23/2016.
 */

public interface WeatherServiceCallback {
    void serviceSuccess(Channel channel);
    void serviceFailure(Exception exception);
}
