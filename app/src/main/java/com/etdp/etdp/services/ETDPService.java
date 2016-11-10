package com.etdp.etdp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class ETDPService extends Service {
	static final public String SERVICE_STATUS_UPDATE = "com.etdp.etdp.ETDPService.SERVICE_STATUS_UPDATE";
	static final public String SERVICE_STATUS = "com.etdp.etdp.ETDPService.SERVICE_STATUS";
	static final public String SERVICE_ETDP = "SERVICE_ETDP";
	public static boolean status;
	private LocalBroadcastManager broadcaster;

	@Override
	public void onCreate() {
		super.onCreate();
		broadcaster = LocalBroadcastManager.getInstance(this);
		ETDPService.status = true;
		Log.d(SERVICE_ETDP, "onCreate()");
		broadcastServiceStatus();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(SERVICE_ETDP, "onStartCommand()");
		return Service.START_STICKY;
	}

	public void broadcastServiceStatus() {
		Intent intent = new Intent(ETDPService.SERVICE_STATUS_UPDATE);
		intent.putExtra(ETDPService.SERVICE_STATUS, ETDPService.status);
		broadcaster.sendBroadcast(intent);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		ETDPService.status = false;
		Log.d(SERVICE_ETDP, "onDestroy()");
		broadcastServiceStatus();
		super.onDestroy();
	}
}
