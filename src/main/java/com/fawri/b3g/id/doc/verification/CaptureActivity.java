package com.fawri.b3g.id.doc.verification;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.fawri.b3g.id.doc.R;


public class CaptureActivity extends AppCompatActivity implements CaptureFaceFragment.CaptureCallback {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_server_capture);

		boolean enrolled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(EnrollmentFragment.ENROLLED, false);
		if (enrolled)
			replaceFragment(new VerificationFragment());
		else
			replaceFragment(new EnrollmentFragment());

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

			case 0:
				return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onCaptureComplete() {


		finish();
	}


	@Override
	public void onCaptureFailed(Bundle info) {


		finish();
	}


	public void replaceFragment(Fragment frgmnt) {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		fragmentTransaction.replace(R.id.fragment, frgmnt);
		fragmentTransaction.commit();
	}

}
