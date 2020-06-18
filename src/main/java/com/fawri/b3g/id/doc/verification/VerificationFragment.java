package com.fawri.b3g.id.doc.verification;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daon.sdk.face.DaonFace;
import com.daon.sdk.face.RecognitionResult;
import com.daon.sdk.face.YUV;
import com.fawri.b3g.id.doc.R;

public class VerificationFragment extends CaptureFaceFragment {

    public static final float THRESHOLD = 1.0f - 0.23297233f;

    public interface VerifyCallback {

        /**
         * Called when an image was enrolled
         */
        void onVerifySucceeded();
        void onVerifyFailed(String message);
    }



    public VerificationFragment() {
        //setOptions(DaonFace.OPTION_LIVENESS|DaonFace.OPTION_QUALITY|DaonFace.OPTION_DEVICE_POSITION);

        // Using client side face recognition
        setOptions(DaonFace.OPTION_LIVENESS| DaonFace.OPTION_QUALITY| DaonFace.OPTION_DEVICE_POSITION| DaonFace.OPTION_RECOGNITION);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_verify, container, false);
    }

    // Timeout
    private int RECOGNITION_TIMEOUT = 7000;
    private boolean timerStarted = false;

    Runnable timeout = new Runnable() {
        @Override
        public void run() {
            showMessage(R.string.face_verify_timeout, false);

            stopCameraPreview();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setInfo(R.string.face_verify_timeout, R.color.red);
                }
            });

            captureFailed(null);
        }
    };


    @Override
    public void onPause() {
        super.onPause();

        stopTimer();
    }

    private void startTimer() {
        if (!timerStarted) {
            timerStarted = true;

            handler.postDelayed(timeout, RECOGNITION_TIMEOUT);
        }
    }

    private void stopTimer() {
        handler.removeCallbacks(timeout);
        timerStarted = false;
    }


    @Override
    protected void onImageAvailable(YUV image, boolean quality) {
        startTimer();
    }

    @Override
    protected void onImageAvailableAndLivenessTimeout() {

        captureFailed(null);
    }

    @Override
    public void onImageAvailableAndLivenessDetected(YUV image) {

        vibrate();

        final Bitmap bmp = getPortraitImage(image);
        if (bmp != null) {
            setPreviewImage(bmp, false);

            verifyImage(bmp, new VerifyCallback() {
                @Override
                public void onVerifySucceeded() {

                    stopTimer();
                    setPreviewImage(bmp, true);
                    showMessage(R.string.face_verify_complete, false);
                    captureComplete();
                }

                @Override
                public void onVerifyFailed(String message) {

                    startCameraPreview();
                    showMessage(message, false);
                }
            });
        } else {
            showMessage(R.string.error_out_of_memory, false);
            captureFailed(null);
        }
    }

    private void verifyImage(Bitmap bmp, final VerifyCallback callback) {


        // Check if we are we using client side recognition
        if ((facesdk.getOptions() & DaonFace.OPTION_RECOGNITION) == DaonFace.OPTION_RECOGNITION) {

            RecognitionResult res = facesdk.recognize(bmp);

            Log.d("DAON", "Client based verification: " + res.getScore());

            if (res.getScore() >= THRESHOLD)
                callback.onVerifySucceeded();
            else
                callback.onVerifyFailed(res.getMessage());

        } else {

            // Make a fake server call

            BusyIndicator.getInstance().setBusy(getActivity());

            // Simulate server call
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    BusyIndicator.getInstance().setNotBusy(getActivity());

                    // In our sample it always succeeds...
                    //noinspection ConstantIfStatement
                    if (true)
                        callback.onVerifySucceeded();
                    else
                        callback.onVerifyFailed(getResources().getString(R.string.face_verify_failed));
                }
            }, 2000);

        }
    }


}
