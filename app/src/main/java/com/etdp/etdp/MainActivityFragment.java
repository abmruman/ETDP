package com.etdp.etdp;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

	public MainActivityFragment() {
	}

	@Override
	public void onStart() {
		super.onStart();
		Snackbar.make(getView(), "Fragment started", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}
}
