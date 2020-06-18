package com.fawri.b3g.id.doc.customoverlay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.widget.Toast;

import com.daon.dmds.managers.DMDSDefaultScanListener;
import com.daon.dmds.models.DMDSOptions;
import com.daon.dmds.models.DMDSResult;
import com.daon.dmds.utils.AppLogger;
import com.daon.dmds.utils.DMDSError;
import com.daon.dmds.views.DaonDocumentScanView;
import com.fawri.b3g.id.doc.BaseActivity;
import com.fawri.b3g.id.doc.R;
import com.fawri.b3g.id.doc.result.ResultActivity;

import java.util.Locale;

public class DefaultScanActivity extends BaseActivity implements DMDSDefaultScanListener {

    private static final String TAG = DefaultScanActivity.class.getSimpleName();
    DaonDocumentScanView daonDocumentScanView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        Intent intent = getIntent();
        DMDSOptions options = intent.getParcelableExtra(DaonDocumentScanView.DMDS_OPTIONS);

        daonDocumentScanView = findViewById(R.id.scan_view);

        try {
            daonDocumentScanView.create(options, this);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (daonDocumentScanView != null) {
            daonDocumentScanView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (daonDocumentScanView != null) {
            daonDocumentScanView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (daonDocumentScanView != null) {
            daonDocumentScanView.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (daonDocumentScanView != null) {
            daonDocumentScanView.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (daonDocumentScanView != null) {
            daonDocumentScanView.destroy();
        }
    }

    @Override
    public void documentScanned(DMDSResult dmdsResult) {
        handleScanResult(dmdsResult);
        writeResults(dmdsResult);
        startActivity(ResultActivity.newStartIntent(DefaultScanActivity.this));

        AppLogger.d(TAG, "--------------------------------------------------------------");
        AppLogger.d(TAG, "DOCUMENT CROPPING RESULTS");
        if (dmdsResult.getDocument().getProcessedImage() != null) {
            AppLogger.d(TAG, String.format(Locale.getDefault(), "Processed image size:\t%dx%d", dmdsResult.getDocument().getProcessedImage().getWidth(),
                    dmdsResult.getDocument().getProcessedImage().getHeight()));
        }

        if (dmdsResult.getDocument().getDocumentType() == null) {
            AppLogger.d(TAG, "Scan failed");
            AppLogger.d(TAG, "Document Capture Type:\t" + dmdsResult.getDocument().getDocumentCaptureType());
            AppLogger.d(TAG, "--------------------------------------------------------------");
            return;
        }

        AppLogger.d(TAG, "Document Type:\t\t" + dmdsResult.getDocument().getDocumentType());
        AppLogger.d(TAG, "Document Capture Type:\t" + dmdsResult.getDocument().getDocumentCaptureType());

        if (dmdsResult.getDocument().getTextExtracted() != null) {
            for (String key : dmdsResult.getDocument().getTextExtracted().keySet()) {
                AppLogger.d(TAG, dmdsResult.getDocument().getDocumentType() + " | " + key + ": " + dmdsResult.getDocument().getTextExtracted().get(key));
            }
        }
        AppLogger.d(TAG, "--------------------------------------------------------------");
        finish();
    }

    @Override
    public void documentScannedFailed(DMDSError dmdsError, DMDSResult dmdsResult) {
        AppLogger.e(TAG, dmdsError.description);

        if (dmdsError != null && !TextUtils.isEmpty(dmdsError.description)) {
            Toast.makeText(this, dmdsError.description, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (daonDocumentScanView != null) {
            daonDocumentScanView.cropActivityResult(requestCode, resultCode, data);
        }
    }

    @VisibleForTesting
    public void handleScanResult(DMDSResult dmdsResult) {
        // handle login response here
    }
}
