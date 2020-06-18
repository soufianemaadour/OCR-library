package com.fawri.b3g.id.doc.verification;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.daon.sdk.face.DaonFace;
import com.daon.sdk.face.Result;
import com.daon.sdk.face.ScoreBuffer;
import com.daon.sdk.face.YUV;
import com.fawri.b3g.id.doc.R;

/**
 *
 */
abstract class FaceAnalysisHandler implements DaonFace.AnalysisCallback {

    private ScoreBuffer<YUV> scoreBuffer = new ScoreBuffer<>(10, 2000);
    private static boolean liveness = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    // Timeout
    private int LIVENESS_TIMEOUT = 3;
    private boolean timerStarted = false;
    private boolean timeout = false;

    private void startTimer() {
        if (!timerStarted) {
            timerStarted = true;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timeout = true;
                }
            }, LIVENESS_TIMEOUT * 1000);
        }
    }


    private boolean isQualityImage(Result result) {
        return result != null &&
                result.isDeviceUpright() &&
                result.getQualityResult().hasAcceptableQuality() &&
                result.getQualityResult().hasAcceptableEyeDistance();
    }

    private int getReason(Result result) {

        if (!result.isDeviceUpright())
            return R.string.face_quality_device_upright;
        else if (!result.getQualityResult().hasUniformLighting())
            return R.string.face_quality_non_uniform_lighting;
        else if (!result.getQualityResult().hasAcceptableExposure())
            return R.string.face_quality_exposure;
        else if (!result.getQualityResult().hasAcceptableFaceAngle())
            return R.string.face_quality_pose;
        else if (result.getQualityResult().getEyeDistance() < 90)
            return R.string.face_quality_too_small;
        else if (result.getQualityResult().getEyeDistance() > 200)
            return R.string.face_quality_too_large;
        else if (!result.getQualityResult().hasAcceptableSharpness())
            return R.string.face_quality_sharpness;

        return R.string.face_quality;
    }


    @Override
    public void onAnalysisError(String message) {

    }

    @Override
    public void onAnalysisData(Bundle data) {

    }

    @Override
    public void onAnalysisResult(YUV image, Result result) {

        if (result.isTrackingFace()) {

            boolean quality = isQualityImage(result);
            if (!quality) {

                onPoorQualityImage(getReason(result));

            } else if (result.getLivenessResult().isLivenessDetected()) {


                if (!liveness) {
                    liveness = true;

                    YUV best = scoreBuffer.getBest();
                    onLivenessDetected(best != null ? best : image);
                }

            } else {
                startTimer();

                liveness = false;

                scoreBuffer.add(image, result.getQualityResult().getBestImageScore());

                onGoodQualityImage(image);
            }

            if (!liveness) {
                int frames = result.getLivenessResult().getNumberOfFrames();
                if (timeout && (frames >= LIVENESS_TIMEOUT * 5))
                    onLivenessTimeout();
            }

            onFaceDetected(image, result.getRecognitionResult().getFacePosition(), quality);
        } else {

            onFaceNotDetected();
        }
    }

    public abstract void onPoorQualityImage(int reason);
    public abstract void onGoodQualityImage(YUV image);
    public abstract void onLivenessDetected(YUV image);
    public abstract void onLivenessTimeout();
    public abstract void onFaceDetected(YUV image, Rect position, boolean quality);
    public abstract void onFaceNotDetected();
}
