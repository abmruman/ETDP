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
import com.etdp.etdp.data.DistanceMatrix;

public class PredictionActivity extends AppCompatActivity {
	private static final String TAG = "PredictionActivity";
	AutoCompleteTextView mStartPointAutoComplete;
	AutoCompleteTextView mDestinationAutoComplete;
	TextView mPredictionResult;
	DistanceMatrix distanceMatrix;
	CustomLocation currentLocation;

	ProgressDialog mProgress;
	String[] startPointList = {"A.B.M Tower, Dhaka, Bangladesh", "PA, United States", "Parana, Brazil",
			"Padua, Italy", "Pasadena, CA", "K F C, 40 Kemal Ataturk Avenue, Dhaka, Bangladesh"};
	String[] destinationList = {"Paries, France", "PA, United States", "Parana, Brazil",
			"K F C, 40 Kemal Ataturk Avenue, Dhaka, Bangladesh", "American International University-Bangladesh, House No. 55/B, Rd No 21, Dhaka 1213, Bangladesh"};

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

				if(originAddress.isEmpty() || destAddress.isEmpty()){
					Toast.makeText(
							PredictionActivity.this,
							"Please enter both addresses.",
							Toast.LENGTH_LONG
					).show();
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
		if(mStartPointAutoComplete.getEditableText().toString().isEmpty() && distanceMatrix != null){
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
				if(forPrediction){
					originAddress = mStartPointAutoComplete.getText().toString();
					destAddress = mDestinationAutoComplete.getText().toString();
					if(originAddress.isEmpty() || destAddress.isEmpty()){
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
				if(forPrediction){
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
				if(mStartPointAutoComplete.getEditableText().toString().isEmpty()){
					mStartPointAutoComplete.setText(distanceMatrix.getFirstOriginAddress());
				}

				if(forPrediction){
					mPredictionResult.append("Prediction Result");
					mPredictionResult.append("\nETA: ");
					mPredictionResult.append(distanceMatrix.getFirstDurationWithUnit());
					mPredictionResult.append(" (from DistanceMatrix API)");
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
