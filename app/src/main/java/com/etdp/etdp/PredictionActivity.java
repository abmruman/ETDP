package com.etdp.etdp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.etdp.etdp.data.CustomLocation;
import com.etdp.etdp.data.DatabaseContract;
import com.etdp.etdp.data.DatabaseHelper;
import com.etdp.etdp.data.DistanceMatrix;
import com.etdp.etdp.data.TravelLog;

import java.util.List;
import java.util.Locale;

public class PredictionActivity extends AppCompatActivity {
	private static final String TAG = "PredictionActivity";
	AutoCompleteTextView mStartPointAutoComplete;
	AutoCompleteTextView mDestinationAutoComplete;
	TextView mPredictionResult;
	DistanceMatrix distanceMatrix;
	CustomLocation currentLocation;

	ProgressDialog mProgress;
	String[] startPointList = {"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh", "American International University-Bangladesh, House No. 55/B, Road No 21, Dhaka 1213, Bangladesh"};
	String[] destinationList = {"Mumtaz Manzil, Muktijoddha Rd, Dhaka, Bangladesh", "American International University-Bangladesh, House No. 55/B, Road No 21, Dhaka 1213, Bangladesh"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prediction);
		mProgress = new ProgressDialog(this);
		mPredictionResult = (TextView) findViewById(R.id.textPredictionResult);

		currentLocation = CustomLocation.fromJson(
				getIntent().getStringExtra(GeoLocationActivity.CURRENT_LOCATION)
		);
		fetchDistanceMatrix(false);


		mStartPointAutoComplete = (AutoCompleteTextView) findViewById(R.id.startPoint);
		mDestinationAutoComplete = (AutoCompleteTextView) findViewById(R.id.destination);

		ArrayAdapter<String> adapterDestination = new ArrayAdapter<>
				(this, android.R.layout.select_dialog_item, destinationList);

		ArrayAdapter<String> adapterStartPoint = new ArrayAdapter<>
				(this, android.R.layout.select_dialog_item, startPointList);

		mStartPointAutoComplete.setThreshold(1);
		mDestinationAutoComplete.setThreshold(1);

		mStartPointAutoComplete.setAdapter(adapterStartPoint);
		mDestinationAutoComplete.setAdapter(adapterDestination);

		findViewById(R.id.getPredictionButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String originAddress = mStartPointAutoComplete.getText().toString();
				String destAddress = mDestinationAutoComplete.getText().toString();

				if (originAddress.isEmpty() || destAddress.isEmpty()) {
					mPredictionResult.setText("Please enter both addresses.");
					return;
				}
				fetchDistanceMatrix(true);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		mDestinationAutoComplete.requestFocus();
		if (mStartPointAutoComplete.getEditableText().toString().isEmpty() && distanceMatrix != null) {
			mStartPointAutoComplete.setText(distanceMatrix.getFirstOriginAddress());

		}
	}

	private void fetchDistanceMatrix(final boolean forPrediction) {
		new AsyncTask<Void, Void, DistanceMatrix>() {
			String originAddress;
			String destAddress;

			@Override
			protected void onPreExecute() {
				mProgress.setMessage("please wait...");
				mProgress.show();
				if (forPrediction) {
					originAddress = mStartPointAutoComplete.getText().toString();
					destAddress = mDestinationAutoComplete.getText().toString();
					if (originAddress.isEmpty() || destAddress.isEmpty()) {
						Toast.makeText(
								PredictionActivity.this,
								"Please enter both addresses.",
								Toast.LENGTH_LONG
						).show();

						cancel(true);
					}
				}
				mProgress.setMessage("please wait...");
				mProgress.show();
			}

			@Override
			protected DistanceMatrix doInBackground(Void... params) {
				if (forPrediction) {
					return DistanceMatrix.fetch(originAddress, destAddress);
				}
				return DistanceMatrix.fetch(currentLocation, currentLocation);
			}

			@Override
			protected void onPostExecute(DistanceMatrix dm) {
				if (dm == null) {
					Toast.makeText(
							PredictionActivity.this,
							getString(R.string.msg_no_results),
							Toast.LENGTH_SHORT
					).show();
					return;
				}
				distanceMatrix = dm;
				if (mStartPointAutoComplete.getEditableText().toString().isEmpty() && distanceMatrix.getStatus().equals(DistanceMatrix.VALID_STATUS)) {
					mStartPointAutoComplete.setText(distanceMatrix.getFirstOriginAddress());
				}
				StringBuilder builder = new StringBuilder();
				builder.append("Prediction Result:");

				if (forPrediction) {
					if (!distanceMatrix.getFirstElementStatus().equals(DistanceMatrix.INVALID_ELEMENT_STATUS)) {
						builder.append("\nETA: ");
						builder.append(distanceMatrix.getFirstDurationWithUnit());
						builder.append(" (DistanceMatrix API)");

						StringBuilder where = new StringBuilder();
						where.append(DatabaseContract.TravelEntry.COLUMN_ORIGIN_ADDRESS)
								.append(" LIKE ?")
								.append(" AND ")
								.append(DatabaseContract.TravelEntry.COLUMN_DEST_ADDRESS)
								.append(" LIKE ?");

						List<TravelLog> travelLogs = TravelLog.readData(
								DatabaseHelper.getDbHelper(PredictionActivity.this),
								null,
								where.toString(),
								new String[]{originAddress, destAddress},
								null,
								"10"
						);

						if (travelLogs.size() < 1) {
							builder.append("\n\n(Not enough data for prediction)");
						} else {
							builder.append("\nPredicted time: ");
							long avg = 0;
							for (TravelLog travelLog : travelLogs) {
								avg += travelLog.travelTime;
							}
							avg /= travelLogs.size();

							int seconds = (int) avg;
							int minutes = seconds / 60;
							seconds = seconds % 60;
							int hours = minutes / 60;
							minutes = minutes % 60;
							if (hours > 0) {
								builder.append(String.format(Locale.ENGLISH, "%d:%02d:%02d hr", hours, minutes, seconds));
							} else {
								builder.append(String.format(Locale.ENGLISH, "%d:%02d min", minutes, seconds));
							}
							builder.append(" (").append(travelLogs.size()).append(") ");
						}
					} else {
						builder.append("\nNo valid results for these addresses.");
					}
					mPredictionResult.setText(builder.toString());
				}

				mProgress.hide();
			}

			@Override
			protected void onCancelled() {
				Toast.makeText(
						PredictionActivity.this,
						getString(R.string.msg_api_request_canceled),
						Toast.LENGTH_SHORT
				).show();
			}
		}.execute();
	}
}
