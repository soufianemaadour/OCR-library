package com.fawri.b3g.id.doc.verification;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daon.sdk.face.DaonFace;
import com.daon.sdk.face.EnrollResult;
import com.daon.sdk.face.YUV;
import com.fawri.b3g.id.doc.R;


public class EnrollmentFragment extends CaptureFaceFragment {

    public static final String ENROLLED = "enrolled";

    private static final int MAX_ATTEMPTS = 3;

    private YUV image;

    private Button takePhotoButton;
    private Button doneButton;
    private int attempt = 1;
    private boolean quality = false;



    public interface EnrollCallback {

        /**
         * Called when an image was enrolled
         */
        void onEnrollSucceeded();
        void onEnrollFailed(String message);
    }

    public EnrollmentFragment() {
        //setOptions(DaonFace.OPTION_QUALITY|DaonFace.OPTION_DEVICE_POSITION);

        // Using client side face recognition
        setOptions(DaonFace.OPTION_QUALITY| DaonFace.OPTION_DEVICE_POSITION| DaonFace.OPTION_RECOGNITION);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enroll, container, false);
        takePhotoButton = view.findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhoto();
            }
        });

        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enroll();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clean up after backgrounding
        if (takePhotoButton != null)
            takePhotoButton.setText(R.string.photo_take);

        if (doneButton != null)
            doneButton.setVisibility(View.GONE);
    }

    @Override
    public void onImageNotAvailable() {
        takePhotoButton.setVisibility(View.GONE);
    }

    @Override
    public void onImageAvailable(YUV image, boolean quality) {
        this.quality = quality;
        this.image = image;
        takePhotoButton.setVisibility(View.VISIBLE);
    }

    private void takePhoto() {

        if (preview.isStopped()) {

            startCameraPreview();

            if (takePhotoButton != null) {
                takePhotoButton.setText(R.string.photo_take);
                takePhotoButton.setVisibility(View.VISIBLE);
            }

            if (doneButton != null)
                doneButton.setVisibility(View.GONE);

        } else {
            stopCameraPreview();
            setPreviewImage(getPortraitImage(image), false);

            if (takePhotoButton != null)
                takePhotoButton.setText(R.string.photo_retake);

            if (doneButton != null)
                doneButton.setVisibility(View.VISIBLE);
        }
    }

    protected void retakePhoto() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takePhoto();
            }
        }, 500);
    }


    protected void enroll()  {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (doneButton != null)
                    doneButton.setVisibility(View.GONE);

                if (takePhotoButton != null)
                    takePhotoButton.setVisibility(View.GONE);
            }
        }, 500);

        if (!quality) {

            showMessage(getResources().getString(R.string.face_quality), false);

            // If we are not able to get the desired image quality in 3 attempts,
            // get the image with the best score.
            if (attempt < MAX_ATTEMPTS) {
                attempt++;
                retakePhoto();
                return;
            }
        }

        showMessage(getResources().getString(R.string.face_enroll), false);

        final Bitmap bmp = getPortraitImage(image);
        if (bmp != null) {
            submitImageToServer(bmp, new EnrollCallback() {
                @Override
                public void onEnrollSucceeded() {

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putBoolean(ENROLLED, true).commit();

                    stopCameraPreview();
                    setPreviewImage(bmp, true);
                    captureComplete();
                }

                @Override
                public void onEnrollFailed(String msg) {
                    showMessage(msg, false);
                    retakePhoto();
                }
            });
        } else {
            showMessage(R.string.error_out_of_memory, false);
            captureFailed(null);
        }

    }

    private void submitImageToServer(Bitmap bmp, final EnrollCallback callback) {

        // Check if we are we using client side recognition
        if ((facesdk.getOptions() & DaonFace.OPTION_RECOGNITION) == DaonFace.OPTION_RECOGNITION) {

            Log.d("DAON", "Client based enrollment");

            EnrollResult er = facesdk.enroll(bmp);
            if (er.isEnrolled())
                callback.onEnrollSucceeded();
            else
                callback.onEnrollFailed(er.getMessage());
        } else {

            // Simulate server call
            BusyIndicator.getInstance().setBusy(getActivity());

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BusyIndicator.getInstance().setNotBusy(getActivity());

                    if (true)
                        callback.onEnrollSucceeded();
                    else
                        callback.onEnrollFailed(getResources().getString(R.string.face_enroll_failed));
                }
            }, 2000);
        }
    }

}
