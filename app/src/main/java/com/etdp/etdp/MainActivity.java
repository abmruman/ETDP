package com.etdp.etdp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.etdp.etdp.data.Condition;
import com.etdp.etdp.data.Item;
import com.etdp.etdp.service.WeatherServiceCallback;
import com.etdp.etdp.service.YahooWeatherService;

import java.nio.channels.Channel;

public class MainActivity extends AppCompatActivity implements WeatherServiceCallback {

    private Condition condition;

    private YahooWeatherService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        service = new YahooWeatherService(this);
        service.refreshWeather("dhaka, bd");
    }


    @Override
    public void serviceSuccess(com.etdp.etdp.data.Channel channel) {
        Item item = channel.getItem();
        Toast.makeText(getApplicationContext(), "Condition: "+item.getCondition().getDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void serviceFailure(Exception exception) {
        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
    }
}
