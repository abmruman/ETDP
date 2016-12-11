package com.etdp.etdp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class PredictionActivity extends AppCompatActivity {
	AutoCompleteTextView mStartPointAutoComplete;
	AutoCompleteTextView mDestinationAutoComplete;

	String[] startPointList = {"A.B.M Tower, Dhaka, Bangladesh", "PA, United States", "Parana, Brazil",
			"Padua, Italy", "Pasadena, CA", "K F C, 40 Kemal Ataturk Avenue, Dhaka, Bangladesh"};
	String[] destinationList = {"Paries, France", "PA, United States", "Parana, Brazil",
			"K F C, 40 Kemal Ataturk Avenue, Dhaka, Bangladesh", "American International University-Bangladesh, House No. 55/B, Rd No 21, Dhaka 1213, Bangladesh"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prediction);

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
	}

	@Override
	protected void onStart() {
		super.onStart();
		mDestinationAutoComplete.requestFocus();
	}
}
