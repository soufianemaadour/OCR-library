package com.fawri.b3g.id.doc.verification;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daon.sdk.face.BitmapTools;
import com.daon.sdk.face.CameraView;
import com.daon.sdk.face.DaonFace;
import com.daon.sdk.face.YUV;
import com.fawri.b3g.id.doc.R;


public abstract class CaptureFaceFragment extends Fragment {

	private static final int width = 640;
	private static final int height = 480;

	public static final int NOTIFICATION_DELAY = 2000;

	private static final int REQUEST_PERMISSIONS = 0;

	public interface CaptureCallback {
		void onCaptureComplete();
		void onCaptureFailed(Bundle info);
	}

	protected CameraView preview = null;
	protected TextView info = null;

	protected FrameLayout cameraOverlayLayout;
	protected ImageView overlay = null;
	protected ImageView qualityIndicator;

	protected CaptureCallback callback;

	protected DaonFace facesdk = null;
	private int options = DaonFace.OPTION_LIVENESS| DaonFace.OPTION_QUALITY| DaonFace.OPTION_DEVICE_POSITION;

	protected Handler handler = new Handler();


	private FaceAnalysisHandler analysisHandler = new FaceAnalysisHandler() {
		@Override
		public void onPoorQualityImage(int reason) {
			setInfo(reason, R.color.white);
			showImageQualityIndicator(false);
		}

		@Override
		public void onGoodQualityImage(YUV image) {
			if (isLivenessEnabled())
				setInfo(R.string.face_liveness_not_detected, R.color.green);
			else
				hideInfo();

			showImageQualityIndicator(true);
		}

		@Override
		public void onLivenessDetected(YUV image) {
			setInfo(R.string.face_liveness_detected, R.color.green);
			onImageAvailableAndLivenessDetected(image);
		}

		@Override
		public void onLivenessTimeout() {
			setInfo(R.string.face_liveness_timeout, R.color.red);
			onImageAvailableAndLivenessTimeout();
		}

		@Override
		public void onFaceDetected(YUV image, Rect position, boolean quality) {
			onImageAvailable(image, quality);
		}

		@Override
		public void onFaceNotDetected() {
			setInfo(R.string.face_not_detected, R.color.white);
			onImageNotAvailable();
		}
	};

	protected void onImageAvailableAndLivenessDetected(YUV image) {

	}

	protected void onImageAvailable(YUV image, boolean quality) {

	}

	protected void onImageNotAvailable() {

	}

	protected void onImageAvailableAndLivenessTimeout() {

	}

	protected void setOptions(int options) {
		this.options = options;
	}

	protected boolean isBlinkEnabled() {
		return (facesdk.getOptions() & DaonFace.OPTION_LIVENESS_BLINK) == DaonFace.OPTION_LIVENESS_BLINK;
	}

	protected boolean isLivenessEnabled() {
		return (facesdk.getOptions() & DaonFace.OPTION_LIVENESS) == DaonFace.OPTION_LIVENESS;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			callback = (CaptureCallback) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement CaptureCallback");
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		facesdk = new DaonFace(getActivity(), options);
	}

	@Override
	public void onStop() {
		super.onStop();
		facesdk.stop();
	}

	private void createPreview() {
		ViewGroup layout = getActivity().findViewById(R.id.preview);
		if (layout != null) {

			// This may be called again, so clean up first
			layout.removeAllViews();

			preview = new CameraView(getActivity());

			cameraOverlayLayout = new FrameLayout(getActivity());

			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT,
					Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

			layoutParams.setMargins(5, 5, 5, 5);
			cameraOverlayLayout.setLayoutParams(layoutParams);

			cameraOverlayLayout.addView(preview);

			int res = getResources().getIdentifier(getActivity().getPackageName() + ":mipmap/preview_overlay", null, null);
			if (res > 0) {
				overlay = new ImageView(getActivity());
				FrameLayout.LayoutParams faceCaptureOverlayLayoutParams = new FrameLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT,
						Gravity.CENTER);

				overlay.setLayoutParams(faceCaptureOverlayLayoutParams);
				overlay.setBackgroundResource(res);

				cameraOverlayLayout.addView(overlay);
			}

			info = new TextView(getActivity());
			FrameLayout.LayoutParams faceCaptureInfoLayoutParams = new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT,
					Gravity.CENTER_HORIZONTAL|Gravity.TOP);
			info.setPadding(10, 25, 10, 25);
			info.setLayoutParams(faceCaptureInfoLayoutParams);
			info.setBackgroundColor(Color.argb(50, 255, 255, 255));
			info.setTypeface(Typeface.DEFAULT_BOLD);
			info.setVisibility(View.GONE);

			cameraOverlayLayout.addView(info);

			qualityIndicator = new ImageView(getActivity());
			FrameLayout.LayoutParams faceCaptureIndicatorLayoutParams = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT,
					Gravity.TOP | Gravity.END);
			qualityIndicator.setPadding(25, 25, 25, 25);
			qualityIndicator.setLayoutParams(faceCaptureIndicatorLayoutParams);
			qualityIndicator.setImageResource(R.drawable.image_quality_indicator);
			qualityIndicator.setVisibility(View.GONE);

			cameraOverlayLayout.addView(qualityIndicator);

			layout.addView(cameraOverlayLayout);
			layout.setVisibility(View.VISIBLE);
		}
	}

	public boolean checkPermissions(String permission) {

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{permission}, REQUEST_PERMISSIONS);
				return false;
			}
		}

		// We have permission...
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		if (requestCode == REQUEST_PERMISSIONS) {

			// We have requested multiple permissions, so all of them need
			// to be checked.

			if (verifyPermissions(grantResults)) {
				startCameraPreview();
			}

		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private boolean verifyPermissions(int[] grantResults) {

		if(grantResults.length < 1)
			return false;

		// Verify that each required permission has been granted, otherwise return false.
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	protected void showMessage(int id, boolean always) {
		if (getActivity() == null)
			return;

		Snackbar sb = Snackbar.make(getActivity().findViewById(android.R.id.content),
				id,
				always ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
		sb.show();
	}

	protected void showMessage(final String message, boolean always) {
		if (getActivity() == null)
			return;

		Snackbar sb = Snackbar.make(getActivity().findViewById(android.R.id.content),
				message,
				always ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
		sb.show();
	}

	protected void vibrate() {
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null && vibrator.hasVibrator())
			vibrator.vibrate(200);
	}


	@Override
	public void onResume() {
		super.onResume();

		if (checkPermissions(Manifest.permission.CAMERA))
			startCameraPreview();
	}

	@Override
	public void onPause() {
		stopCameraPreview();
		super.onPause();
	}


	@SuppressWarnings("deprecation")
	protected void setPreviewFrameCapture(boolean on) {

		if (preview == null)
			return;
		
		if (!on) {
			preview.setPreviewFrameCallbackWithBuffer(null);
		} else {
			preview.setPreviewFrameCallbackWithBuffer(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					if (data != null) {
						facesdk.analyze(data, analysisHandler);
						preview.addPreviewFrameBuffer(data);
					}
				}
			});	
		}
	}

	protected Bitmap getPortraitImage(YUV image) {
		try {
			if (image != null)
				return BitmapTools.rotate(image.toBitmap(), preview.getDegreesToRotate(), true);
		} catch (OutOfMemoryError e) {
			// May happen on older devices depending on resolution and number of images
			// in the score buffer.
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	protected void setInfo(int resid, int color) {
		if (getActivity() != null) {
			info.setText(resid);
			info.setTextColor(getResources().getColor(color));
			info.setVisibility(View.VISIBLE);
		}
	}

	protected void hideInfo() {
		info.setVisibility(View.GONE);
	}



	private void showImageQualityIndicator(boolean show) {
		if (qualityIndicator != null)
			qualityIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	protected void setPreviewImage(Bitmap bmp, boolean checked) {

		setPreviewFrameCapture(false);

		if (bmp != null && getActivity() != null) {
			ViewGroup layout = getActivity().findViewById(R.id.preview);
			if (layout != null) {

				ImageView photo = new ImageView(getActivity());

				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT,
						Gravity.CENTER);
				params.setMargins(5, 5, 5, 5);
				photo.setLayoutParams(params);
				photo.setScaleType(ImageView.ScaleType.FIT_XY);
				photo.setImageBitmap(bmp);

				layout.addView(photo);

				if (checked) {
					ImageView check = new ImageView(getActivity());
					params = new FrameLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT,
							Gravity.BOTTOM | Gravity.END);
					check.setLayoutParams(params);
					check.setImageResource(R.mipmap.verified);

					layout.addView(check);
				}
			}
		}
	}

	protected void enableOverlay(boolean enable) {
		if (overlay != null)
			overlay.setVisibility(enable ? View.VISIBLE : View.GONE);
	}



	@SuppressWarnings("deprecation")
	public void startCameraPreview() {

		createPreview();

		// Start camera preview
		Camera.Size size = preview.start(getActivity(), width, height);

		facesdk.setImageSize(size.width, size.height);

		// Set preview frame callback and start collecting frames.
		// Frames are in the NV21 format YUV encoding.
		setPreviewFrameCapture(true);
	}

	public void stopCameraPreview() {

		if (preview != null)
			preview.stop();

		setPreviewFrameCapture(false);

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				showImageQualityIndicator(false);
				enableOverlay(false);
			}
		});
	}

	protected void captureComplete() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				callback.onCaptureComplete();
			}
		}, NOTIFICATION_DELAY);
	}

	protected void captureFailed(final Bundle info) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				callback.onCaptureFailed(info);
			}
		}, NOTIFICATION_DELAY);
	}

}
