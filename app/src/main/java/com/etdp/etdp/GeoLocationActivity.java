package com.etdp.etdp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.etdp.etdp.data.CustomLocation;
import com.etdp.etdp.data.DistanceMatrix;
import com.etdp.etdp.data.Weather;

import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GeoLocationActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
	static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1004;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int TEN_MINUTES = 1000 * 60 * 10;
	private static final String COUNTER_START_TIME = "COUNTER_START_TIME";
	private static final String COUNTER_END_TIME = "COUNTER_END_TIME";
	private static final String IS_MONITORING = "IS_MONITORING";
	private static final String START_LOCATION = "START_LOCATION";
	private static final String END_LOCATION = "END_LOCATION";
	private static final String WEATHER = "WEATHER";
	private static final String DISTANCE_MATRIX = "DISTANCE_MATRIX";

	private final String TAG = "GeoLocationActivity";
	SharedPreferences sharedPref;
	ProgressDialog mProgress;
	// Acquire a reference to the system Location Manager
	private LocationManager locationManager;
	private Criteria criteria;
	// Define a listener that responds to location updates
	private LocationListener locationListener;

	private ToggleButton mToggleMonitorButton;
	private TextView mTimerText;

	private long diffTime;
	private boolean isMonitoring;
	private Thread timerThread;
	private Location currentLocation;
	private Location originLocation;
	private Location destLocation;
	private Long startTime;
	private Long endTime;
	private Weather weather;
	private DistanceMatrix distanceMatrix;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_geo_location);

		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.msg_please_wait));

		mTimerText = (TextView) findViewById(R.id.textViewTimer);
		mToggleMonitorButton = (ToggleButton) findViewById(R.id.buttonMonitor);
		/***
		 *
		 * Shared Preference for temporary data.
		 *
		 ***/
		sharedPref = getPreferences(Context.MODE_PRIVATE);
		startTime = sharedPref.getLong(COUNTER_START_TIME, 0);
		endTime = sharedPref.getLong(COUNTER_END_TIME, 0);
		isMonitoring = sharedPref.getBoolean(IS_MONITORING, false);
		originLocation = CustomLocation.fromJsonToLocation(sharedPref.getString(START_LOCATION, null));
		destLocation = CustomLocation.fromJsonToLocation(sharedPref.getString(END_LOCATION, null));
		weather = Weather.fromJson(sharedPref.getString(WEATHER, null));
		distanceMatrix = DistanceMatrix.fromJson(sharedPref.getString(DISTANCE_MATRIX, null));

		try {
			Log.d(TAG, "onCreate: StartLocation: " + originLocation.toString());
			Log.d(TAG, "onCreate: EndLocation: " + destLocation.toString());
		} catch (Exception e) {
			Log.e(TAG, "onCreate: " + e.toString());
		}
		/***
		 *
		 * Sets Button state & ClickListener
		 *
		 ***/
		if (isMonitoring) {
			mToggleMonitorButton.setChecked(true);
		} else {
			mToggleMonitorButton.setChecked(false);
		}

		mToggleMonitorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isMonitoring) {
					if (stopMonitoring())
						mToggleMonitorButton.setChecked(false);
					else
						mToggleMonitorButton.setChecked(true);
				} else {
					if (startMonitoring())
						mToggleMonitorButton.setChecked(true);
					else
						mToggleMonitorButton.setChecked(false);
				}
			}
		});
		/***
		 *
		 * Calls Location API
		 *
		 ***/
		// Register the listener with the Location Manager to receive location updates
		/*** Location Manager & Listener ***/
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		/*** System will choose a provider according to this Criteria  ***/
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		criteria.setBearingRequired(true);
//		criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
// 		criteria.setSpeedRequired(true);
//		criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);

		/*** use Network location data: ***/
		//locationProvider = LocationManager.NETWORK_PROVIDER;
		/*** Or, use GPS location data: ***/
		//locationProvider = LocationManager.GPS_PROVIDER;

		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				Toast.makeText(
						GeoLocationActivity.this,
						"onLocationChanged",
						Toast.LENGTH_SHORT
				).show();
				if (isBetterLocation(location, currentLocation))
					saveLocations(location);
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				Toast.makeText(GeoLocationActivity.this, "onStatusChanged", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProviderEnabled(String provider) {
				Toast.makeText(GeoLocationActivity.this, "onProviderEnabled", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProviderDisabled(String provider) {
				Toast.makeText(GeoLocationActivity.this, "onProviderDisabled", Toast.LENGTH_SHORT).show();
			}
		};
	}

	@Override
	protected void onStart() {
		super.onStart();
		/***
		 *
		 * Timer Thread
		 *
		 ***/
		timerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (isMonitoring) {
						mToggleMonitorButton.post(new Runnable() {
							public void run() {
								diffTime = System.currentTimeMillis() - startTime;
								int seconds = (int) (diffTime / 1000);
								int minutes = seconds / 60;
								seconds = seconds % 60;
								int hours = minutes / 60;
								minutes = minutes % 60;
								if (hours > 0) {
									mTimerText.setText(String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds));
								} else {
									mTimerText.setText(String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds));
								}
								mTimerText.setVisibility(View.VISIBLE);
							}
						});
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		timerThread.start();
	}

	@Override
	protected void onStop() {
		//removeLocationRequest();
		timerThread.interrupt();
		super.onStop();
	}

	public boolean startMonitoring() {
		statusCheck();
		if (requestSingleLocationUpdate()) {
			//Intent intent = new Intent(this, ETDPService.class);
			//intent.putExtra("Source", "MainActivity");
			//startService(intent);
			mProgress.setMessage(getString(R.string.msg_updating_location));
			mProgress.show();

			startTime = System.currentTimeMillis();
			isMonitoring = true;

			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putLong(COUNTER_START_TIME, startTime);
			editor.putBoolean(IS_MONITORING, isMonitoring);
			editor.apply();
			return true;
		}
		return false;
	}

	public boolean stopMonitoring() {
		statusCheck();
		if (requestSingleLocationUpdate()) {
			//Intent intent = new Intent(this, ETDPService.class);
			//intent.putExtra("Source", "MainActivity");
			//stopService(intent);
			mProgress.setMessage(getString(R.string.msg_updating_location));
			mProgress.show();

			mTimerText.setVisibility(View.INVISIBLE);
			endTime = System.currentTimeMillis();
			isMonitoring = false;

			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putLong(COUNTER_END_TIME, endTime);
			editor.putBoolean(IS_MONITORING, isMonitoring);
			editor.apply();

			return true;
		}
		return false;
	}

	private boolean requestSingleLocationUpdate() {
		if (checkPermission()) {
			try {
				//locationManager.requestSingleLocationUpdate(10000, 0, criteria, locationListener, null);
				//locationManager.requestSingleLocationUpdate(locationProvider, 10000, 0, locationListener);
				locationManager.requestSingleUpdate(criteria, locationListener, null);
				return true;
			} catch (Exception e) {
				// uh oh. GPS is probably off
				e.printStackTrace();
				Toast.makeText(this, R.string.msg_location_request_failed, Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}

	private void requestLocationUpdates() {
		if (checkPermission()) {
			try {
				locationManager.requestLocationUpdates(15000, 0, criteria, locationListener, null);
			} catch (Exception e) {
				// uh oh. GPS is probably off
				e.printStackTrace();
				Toast.makeText(
						this,
						R.string.msg_location_request_failed,
						Toast.LENGTH_LONG
				).show();
			}
		}
	}

	private void removeLocationRequest() {
		if (checkPermission()) {
			locationManager.removeUpdates(locationListener);
		}
	}

	private void saveLocations(Location location) {
		mProgress.hide();
		String state;
		if (location == null) {
			Toast.makeText(this, "Location: " + "NULL", Toast.LENGTH_LONG).show();
			return;
		}

		currentLocation = location;
		String jsonLocation = CustomLocation.toString(location);
		SharedPreferences.Editor editor = sharedPref.edit();

		Log.d(TAG, "saveLocations: " + jsonLocation);

		if (isMonitoring) {
			originLocation = location;
			state = "Start: ";
			editor.putString(START_LOCATION, jsonLocation);

			long diff = 0;
			if (weather == null ||
					(diff = System.currentTimeMillis() / 1000 - weather.getTimestamp()) >= TEN_MINUTES) {
				Log.d(TAG, "saveLocations: Time: " + diff);
				fetchWeather();
			} else {
				Toast.makeText(
						GeoLocationActivity.this,
						"Weather is up-to-date: " + weather.getFirstCondition(),
						Toast.LENGTH_SHORT).show();
				Log.d(TAG, "saveLocations: Weather up-to-date(" + weather.getFirstCondition() + ")");
			}
		} else {
			if (originLocation != null) {
				destLocation = location;
				state = "end: ";
				editor.putString(END_LOCATION, jsonLocation);
				if (destLocation != null)
					fetchDistanceMatrix();
			} else {
				state = "Current: ";
			}
		}
		editor.apply();
		Toast.makeText(
				this,
				state + location.toString(),
				Toast.LENGTH_LONG
		).show();

		try {
			CustomLocation customLocation = new CustomLocation(location);
			Log.d(TAG, "saveLocations: customLocation: " + customLocation.toString());
			Location tLoc = new Location(LocationManager.GPS_PROVIDER);
			Log.d(TAG, "saveLocations: customLocation: " + tLoc.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fetchDistanceMatrix() {
		mProgress.setMessage(getString(R.string.msg_calling_DM_API));
		new AsyncTask<Void, Void, DistanceMatrix>() {
			@Override
			protected void onPreExecute() {
				mProgress.show();
			}

			@Override
			protected DistanceMatrix doInBackground(Void... params) {
				return DistanceMatrix.fetch(originLocation, destLocation);
			}

			@Override
			protected void onPostExecute(DistanceMatrix dm) {
				mProgress.hide();
				if (dm == null) {
					Toast.makeText(GeoLocationActivity.this, R.string.msg_no_results, Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					distanceMatrix = dm;
					Toast.makeText(GeoLocationActivity.this, "API Status: " + distanceMatrix.getStatus(), Toast.LENGTH_SHORT).show();

					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(DISTANCE_MATRIX, distanceMatrix.toString());
					editor.apply();

					//Examples for accessing DistanceMatrix Object.
					Log.d(TAG, "onPostExecute: " + distanceMatrix.getStatus());
					Log.d(TAG, "onPostExecute: " + distanceMatrix.toString());

					Log.d(TAG, "onPostExecute: " + distanceMatrix.getFirstOriginAddress());
					Log.d(TAG, "onPostExecute: " + distanceMatrix.getFirstDestinationAddress());

					Log.d(TAG, "onPostExecute: " + distanceMatrix.getFirstElementStatus());

					Log.d(TAG, "onPostExecute: " + distanceMatrix.getFirstDistanceValue());
					Log.d(TAG, "onPostExecute: " + distanceMatrix.getFirstDistanceWithUnit());

					Log.d(TAG, "onPostExecute: " + distanceMatrix.getFirstDurationValue());
					Log.d(TAG, "onPostExecute: " + distanceMatrix.getFirstDurationWithUnit());
				} catch (Exception e) {
					Log.e(TAG, "onPostExecute: " + e.toString());
				}
			}

			@Override
			protected void onCancelled() {
				mProgress.hide();
				Toast.makeText(GeoLocationActivity.this, R.string.msg_api_request_canceled, Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}

	private void fetchWeather() {
		mProgress.setMessage(getString(R.string.msg_calling_W_API));
		new AsyncTask<Void, Void, Weather>() {
			@Override
			protected void onPreExecute() {
				mProgress.show();
			}

			@Override
			protected Weather doInBackground(Void... params) {
				return Weather.fetch(currentLocation);
			}

			@Override
			protected void onPostExecute(Weather w) {
				mProgress.hide();
				if (w == null) {
					Toast.makeText(GeoLocationActivity.this, R.string.msg_no_results, Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					if (w.getStatus() == 200) {
						weather = w;
						Toast.makeText(GeoLocationActivity.this, "Updated weather: " + weather.getFirstCondition(), Toast.LENGTH_SHORT).show();

						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString(WEATHER, weather.toString());
						editor.apply();

						//Examples for accessing DistanceMatrix Object.
						Log.d(TAG, "onPostExecute: " + weather.toString());
						Log.d(TAG, "onPostExecute: Updated weather: " + weather.getFirstCondition());
						Log.d(TAG, "onPostExecute: " + weather.getStatus());
						Log.d(TAG, "onPostExecute: " + weather.getTimestamp());
					} else {
						Toast.makeText(GeoLocationActivity.this, R.string.msg_invalid_weather_response, Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Log.e(TAG, "onPostExecute: ", e);
				}
			}

			@Override
			protected void onCancelled() {
				mProgress.hide();
				Toast.makeText(GeoLocationActivity.this, R.string.msg_api_request_canceled, Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 * @param location            The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		// A new location is always better than no location
		if (currentBestLocation == null) return true;

		// Current location is always better than no location
		if (location == null) return false;

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) return true;
		// If the new location is more than two minutes older, it must be worse
		if (isSignificantlyOlder) return false;

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider =
				isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		return isMoreAccurate
				|| (isNewer && !isLessAccurate)
				|| (isNewer && !isSignificantlyLessAccurate && isFromSameProvider);
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

	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {
//		requestSingleLocationUpdate();
	}

	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(
				requestCode, permissions, grantResults, this);
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.msg_turn_gps_on))
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public void statusCheck() {
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}
	}

	@AfterPermissionGranted(REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
	private boolean checkPermission() {
		if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			return true;
		}
		EasyPermissions.requestPermissions(
				this,
				getString(R.string.msg_location_access_required),
				REQUEST_PERMISSION_ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION
		);
		return false;
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
