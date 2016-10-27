package com.etdp.etdp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.etdp.etdp.services.ETDPService;

public class MainActivity extends AppCompatActivity {
	FloatingActionButton fab;
	BroadcastReceiver receiverServiceStatus;
	TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		textView = (TextView) findViewById(R.id.textView);
		fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int status;
				if (toggleMainService()) {
					status = R.string.mainServiceStarted;
				} else {
					status = R.string.mainServiceStopped;
				}
				Snackbar.make(view, status, Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
				//changeFabStatus(ETDPService.status);
			}
		});
		receiverServiceStatus = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Boolean aBoolean = intent.getBooleanExtra(ETDPService.SERVICE_STATUS, false);
				changeFabStatus(aBoolean);
				if (aBoolean) {
					textView.setText(R.string.mainServiceStarted);
				} else {
					textView.setText(R.string.mainServiceStopped);
				}
			}
		};
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiverServiceStatus,
				new IntentFilter(ETDPService.SERVICE_STATUS_UPDATE)
		);
		changeFabStatus(isTheServiceRunning(ETDPService.class));
	}

	public void changeFabStatus(Boolean aBoolean) {
		if (aBoolean) {
			fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
		} else {
			fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
		}
	}

	/***
	 * Returns True if starting service, False if Stopping
	 ***/
	private Boolean toggleMainService() {
		Boolean status = isTheServiceRunning(ETDPService.class);
		if (status) {
			stopMainService();
		} else {
			startMainService();
		}
		return !status;
	}

	public void startMainService() {
		Intent intent = new Intent(this, ETDPService.class);
		intent.putExtra("Source", "MainActivity");
		startService(intent);
	}

	public void stopMainService() {
		Intent intent = new Intent(this, ETDPService.class);
		intent.putExtra("Source", "MainActivity");
		stopService(intent);
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverServiceStatus);
		super.onStop();
	}

	private boolean isTheServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
