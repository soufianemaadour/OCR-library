package com.fawri.b3g.id.doc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;


public class OverlayView extends TextureView implements TextureView.SurfaceTextureListener {


    public static int OVAL_TRANSPARENT = 0;
    public static int OVAL_COLOR = 1;
    public static int OVAL_NONE = 2;

    public static int OVERLAY_NONE = 0;
    public static int OVERLAY_GREEN = 1;
    public static int OVERLAY_RED = 2;

    private int ovalBackgroundColor;
    private int ovalType = OVAL_NONE;
    private int overlayType = OVERLAY_NONE;

    private Bitmap overlay_green;
    private Bitmap overlay_red;

    private Bitmap overlay_cropped;
    private float[] overlay_resized_offset;

    private Animation anim;

    private int ringColor = Color.TRANSPARENT;
    private int overlayTransparency;


    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        setSurfaceTextureListener(this);
        setOpaque(false);

        ovalBackgroundColor = Color.TRANSPARENT;
        ovalType = OVAL_TRANSPARENT;
        ringColor = Color.TRANSPARENT;

        overlay_green = BitmapFactory.decodeResource(getResources(), R.drawable.frame_overlay_far_green);
        overlay_red = BitmapFactory.decodeResource(getResources(), R.drawable.frame_overlay_far_red);

        overlayTransparency = Math.round(255*0.75f);

    }

    public void setOvalType(int sOvalType){
        if (ovalType != sOvalType){
            ovalType = sOvalType;
            updateDrawing(false);
        }
    }

    public void setOvalBackgroundColor(int color) {
        if (ovalBackgroundColor != color) {
            ovalBackgroundColor = color;
            updateDrawing(false);
        }
    }

    public void setOverlayType(int overlayType) {

        if (this.overlayType != overlayType) {
            this.overlayType = overlayType;
            updateDrawing(true);
        }
    }

    private synchronized void updateDrawing(boolean initialize) {

//        if (overlayType == OVERLAY_NONE)
//            return;

        Canvas canvas = lockCanvas();

        if (initialize) {

            try {
                Bitmap overlay = overlayType == OVERLAY_RED ? overlay_red : overlay_green;

                float ovalFaceToWidthRatio = 0.43f;
                float screenFaceToWidthRatio = 0.64f;
                float scale = canvas.getWidth() * screenFaceToWidthRatio / (overlay.getWidth() * ovalFaceToWidthRatio);

                Matrix m = new Matrix();
                m.setTranslate(canvas.getWidth() / 2f - overlay.getWidth() / 2f, canvas.getHeight() / 2f - overlay.getHeight() / 2f);
                m.postScale(1f * scale, 1.4f * scale, canvas.getWidth() / 2f, canvas.getHeight() / 2f);

                overlay_resized_offset = new float[2];
                m.mapPoints(overlay_resized_offset, new float[]{0, 0});

                Bitmap overlay_resized = Bitmap.createBitmap(overlay, 0, 0, overlay.getWidth(), overlay.getHeight(), m, false);

                overlay_cropped = Bitmap.createBitmap(overlay_resized,
                        Math.round(-overlay_resized_offset[0]),
                        Math.round(-overlay_resized_offset[1]),
                        canvas.getWidth(), canvas.getHeight());
                overlay_resized.recycle();

            } catch (OutOfMemoryError e) {
                Log.d(Class.class.getName(), e.getLocalizedMessage());

                overlay_cropped = null;
                setRingColor(Color.GREEN);
            }

        }

        if (canvas != null) {
            canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);

            drawOvalHole(canvas);

            drawOverlay(canvas);

            unlockCanvasAndPost(canvas);
        }
    }

    public synchronized void startOvalAnimation(long durationMs, Animation.AnimationListener listener){

        anim = new ScaleAnimation(
                1f, 1.5f, // Start and end values for the X axis scaling
                1f, 1.5f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(durationMs);
        anim.setAnimationListener(listener);
        startAnimation(anim);
    }

    public synchronized void stopOvalAnimation(){
        if (anim != null) {
            anim.cancel();
            anim.reset();
            anim = null;
        }
    }

    public void setOvalRotationX(float angle){
        setRotationX(angle);
        setRotationY(0);
    }


    private void drawOvalHole(Canvas canvas){

        if (overlayType == OVERLAY_NONE)
            return;

        canvas.drawColor(ovalBackgroundColor);

        int height = canvas.getHeight();
        int width = canvas.getWidth();

        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        int left = (int) (width - width * 0.70);
        int right = (int) (width - width * 0.30);

        int top = (int) (height - height * 0.28);
        int bottom = (int) (height - height * 0.28 - ((right - left) * 1.5));//(int) (height - height * 0.85);

        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawOval(rect, p);

        if (ovalType == OVAL_COLOR) {

            int holeColor = Color.argb(200,
                    Color.red(ovalBackgroundColor),
                    Color.green(ovalBackgroundColor),
                    Color.blue(ovalBackgroundColor));
            Paint p1 = new Paint();
            p1.setColor(holeColor);
            p1.setStyle(Paint.Style.FILL);
            canvas.drawOval(rect, p1);
        }

        Paint ringPaint = new Paint();
        ringPaint.setColor(ringColor);
        ringPaint.setStyle(Paint.Style.STROKE);
        canvas.drawOval(rect, ringPaint);
    }

    private void drawOverlay(Canvas canvas) {

        if (overlayType == OVERLAY_NONE)
            return;

        if (overlay_cropped != null) {
            Paint p = new Paint();
            p.setAlpha(overlayTransparency);
            canvas.drawBitmap(overlay_cropped, 0, 0, p);
        }
    }

    public void setRingColor(int ringColour) {
        if (this.ringColor != ringColour) {
            this.ringColor = ringColour;
            updateDrawing(false);
        }
    }

    public void clear() {
        ovalType = OVAL_NONE;
        ovalBackgroundColor = Color.TRANSPARENT;
        ringColor = Color.TRANSPARENT;
        updateDrawing(false);
    }


    public void setOverlayTransparency(float alpha) {
        overlayTransparency = Math.round(255*alpha);
        updateDrawing(false);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        updateDrawing(true);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
}
