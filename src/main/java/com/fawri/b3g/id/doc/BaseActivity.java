package com.fawri.b3g.id.doc;

import android.support.v7.app.AppCompatActivity;

import com.daon.dmds.models.DMDSResult;

public class BaseActivity extends AppCompatActivity {

    private ResultStorage resultStorage;


    private void setupStorageListener() {
        if (getApplication() instanceof ResultStorage) {
            resultStorage = (ResultStorage) getApplication();
        }
    }

    public DMDSResult readResults() {
        if (resultStorage == null) {
            setupStorageListener();
        }
        return resultStorage.getResults();
    }

    public void writeResults(DMDSResult results) {
        if (resultStorage == null) {
            setupStorageListener();
        }
        resultStorage.setResults(results);
    }
}
