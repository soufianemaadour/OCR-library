package com.fawri.b3g.id.doc;

import android.app.Application;

import com.daon.dmds.models.DMDSResult;

public class App extends Application implements ResultStorage {

    private static DMDSResult scanResult;

    @Override
    public DMDSResult getResults() {
        return scanResult;
    }

    @Override
    public void setResults(DMDSResult results) {
        scanResult = results;
    }
}
