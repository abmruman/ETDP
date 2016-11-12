package com.etdp.etdp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GeoLocationActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

	static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1004;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final String COUNTER_START_TIME = "COUNTER_START_TIME";
	private static final String COUNTER_END_TIME = "COUNTER_END_TIME";
	private static final String IS_MONITORING = "IS_MONITORING";

	SharedPreferences sharedPref;
	// Acquire a reference to the system Location Manager
	private LocationManager locationManager;
	// Define a listener that responds to location updates
	private LocationListener locationListener;

	//private Button mStartMonitorButton;
	private ToggleButton mToggleMonitorButton;

	/* Declaration of predict time button */
	private Button mPredictTimeButton;

	private long diffTime;
	private Location startLocation;
	private Location endLocation;
	private Long startTime;
	private Long endTime;
	private Boolean isMonitoring;
	private Thread timerThread;
	private TextView mTimerText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geo_location);

		//mStartMonitorButton = (Button) findViewById(R.id.monitorButton);
        mTimerText = (TextView) findViewById(R.id.textViewTimer);
		mToggleMonitorButton = (ToggleButton) findViewById(R.id.buttonMonitor);

		/* Initialization of mPredictTimeButton */
		mPredictTimeButton = (Button) findViewById(R.id.predictButton);

		sharedPref = getPreferences(Context.MODE_PRIVATE);
		startTime = sharedPref.getLong(COUNTER_START_TIME, 0);
		endTime = sharedPref.getLong(COUNTER_END_TIME, 0);
		isMonitoring = sharedPref.getBoolean(IS_MONITORING, false);

		if(isMonitoring){
			mToggleMonitorButton.setChecked(true);
		}else{
			mToggleMonitorButton.setChecked(false);
		}

		/* Implementation of mPredictTimeButton */
		mPredictTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(GeoLocationActivity.this, PredictActivity.class);
				startActivity(intent);
			}
		});
		mToggleMonitorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isMonitoring){
					startMonitoring();
				}else{
					stopMonitoring();
				}
			}
		});

		timerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (isMonitoring) {
						mToggleMonitorButton.post(new Runnable() {
							public void run() {
								diffTime = System.currentTimeMillis() - startTime;
								int seconds = (int) (diffTime / 1000);
								int minutes = seconds / 60;
								seconds = seconds % 60;
								int hours = minutes / 60;
								minutes = minutes % 60;
								if(hours>0){
									//mStartMonitorButton.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
								    mTimerText.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
                                }else {
									//mStartMonitorButton.setText(String.format("%d:%02d", minutes, seconds));
                                    mTimerText.setText(String.format("%d:%02d", minutes, seconds));
								}
                                mTimerText.setVisibility(View.VISIBLE);
							}
						});
					}
				}
			}
		});
		timerThread.start();

		// Register the listener with the Location Manager to receive location updates
		/** Location Manager & Listener **/
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				// TODO: Use Location Only when needed
				useNewLocation(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		startListening();
	}

	private boolean startListening() {
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		/*** Or, use GPS location data: ***/
		// String locationProvider = LocationManager.GPS_PROVIDER;

		if (ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			checkPermission();
			return false;
		}
		statusCheck();
		locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
		return true;
	}

	private Location lastKnownLocation() {
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		// Or use LocationManager.GPS_PROVIDER
		Location lastKnownLocation;

		if (ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			checkPermission();
			return null;
		}
		statusCheck();
		return locationManager.getLastKnownLocation(locationProvider);
	}


	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 * @param location            The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two providers are the same
	 */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	private void useNewLocation(Location location) {
		Toast.makeText(this, "ETDP: " + location.toString(), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {

	}

	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(
				requestCode, permissions, grantResults, this);
	}

	public void statusCheck() {
		//final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			buildAlertMessageNoGps();
		}
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@AfterPermissionGranted(REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
	private void checkPermission() {
		if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			EasyPermissions.requestPermissions(this, "This app needs to access your location", REQUEST_PERMISSION_ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
		}
	}


	public void startMonitoring() {
		if (!isMonitoring) {
			/*Intent intent = new Intent(this, ETDPService.class);
			//intent.putExtra("Source", "MainActivity");
			startService(intent);*/

			startTime = System.currentTimeMillis();
			isMonitoring = true;

			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putLong(COUNTER_START_TIME, startTime);
			editor.putBoolean(IS_MONITORING, isMonitoring);
			editor.commit();
		}
	}

	public void stopMonitoring() {
		/*Intent intent = new Intent(this, ETDPService.class);
		//intent.putExtra("Source", "MainActivity");
		stopService(intent);*/
        mTimerText.setVisibility(View.INVISIBLE);
		endTime = System.currentTimeMillis();
		isMonitoring = false;

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(COUNTER_END_TIME, endTime);
		editor.putBoolean(IS_MONITORING, isMonitoring);
		editor.commit();
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
