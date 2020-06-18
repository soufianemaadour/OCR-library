package com.fawri.b3g.id.doc.customoverlay;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daon.dmds.managers.DMDSCustomScanListener;
import com.daon.dmds.models.DMDSDocumentDetectionMetadata;
import com.daon.dmds.models.DMDSMrzDetectionMetadata;
import com.daon.dmds.models.DMDSOptions;
import com.daon.dmds.models.DMDSOrientation;
import com.daon.dmds.models.DMDSQuadrangle;
import com.daon.dmds.models.DMDSRect;
import com.daon.dmds.models.DMDSResult;
import com.daon.dmds.models.DocTypeEnum;
import com.daon.dmds.utils.AppLogger;
import com.daon.dmds.utils.DMDSError;
import com.daon.dmds.views.DaonDocumentScanView;
import com.fawri.b3g.id.doc.BaseActivity;
import com.fawri.b3g.id.doc.R;
import com.fawri.b3g.id.doc.result.ResultActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomScanActivity extends BaseActivity implements DMDSCustomScanListener {
    public static final String LICENSE_KEY = "sRwAAAAUY29tLmZhd3JpLmIzZy5pZC5kb2O3ToHU/cZ/5+tvXKAWVXFxLgkbkl92FXLSBic8TcDffgEH++IZfLahxzTSORh0L5XLVTVH5q93fAud5KdNYuHTy+uJW08wCHntNU2ZlVQ06Eptr3w3AEHAokBrsBWA6jYqUYwSv5iSYF+oLkI7N0p4peyGw/12tWNs/TF+Nqp0knL0BYPikf0GdQtVuUN9wrT7+t2tQ1856Hw24lHXS0Mdhs/vs61a9fvmEM0Wui7jvKqIYncilJeQFpYEKf57x478V5qYo9rWsVVuVU7l+DY=";

    private static final String TAG = CustomScanActivity.class.getSimpleName();
    DaonDocumentScanView dmdsBaseScanView;
    ImageView scanningView;
    ImageView backImageView;

    Handler handler;
    Handler messageHandler;

    TextView msgContent;
    TextView msgHeader;
    TextView btnScanAgain;
    TextView btnDetails;
    TextView btnScan;
    TextView btnCancel;


    TextView upperLeftDot;
    TextView upperRightDot;
    TextView lowerLeftDot;
    TextView lowerRightDot;

    LinearLayout messageContainer;

    int heightScreen = 0;
    int widthScreen = 0;

    String glareMsg = "";
    Bitmap frontImage;

    List<DocTypeEnum> initialDocTypes;
    private DMDSOptions options;
    private List<DocTypeEnum> supportedDocumentList;

    Runnable infoMessage = new Runnable() {
        @Override
        public void run() {
            String currentText = msgContent.getText().toString();
            if (currentText.contentEquals(getString(R.string.msg_scanning_1))) {
                msgContent.setText(getString(R.string.msg_scanning_2));
            } else if (currentText.contentEquals(getString(R.string.msg_scanning_2))) {
                msgContent.setText(getString(R.string.msg_scanning_3));
            } else if (currentText.contentEquals(getString(R.string.msg_scanning_3))) {
                msgContent.setText(getString(R.string.msg_scanning_1));
            }
            if (messageHandler != null) {
                messageHandler.postDelayed(this, 400);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        dmdsBaseScanView = findViewById(R.id.custom_scan_view);

        btnScan = findViewById(R.id.btn_scan);
        btnScanAgain = findViewById(R.id.btn_scan_again);
        btnDetails = findViewById(R.id.btn_details);
        btnCancel = findViewById(R.id.btn_cancel);

        msgContent = findViewById(R.id.message_content);
        msgHeader = findViewById(R.id.message_header);
        upperLeftDot = findViewById(R.id.upper_left_dot);
        upperRightDot = findViewById(R.id.upper_right_dot);
        lowerLeftDot = findViewById(R.id.lower_left_dot);
        lowerRightDot = findViewById(R.id.lower_right_dot);

        scanningView = findViewById(R.id.scanning_rect);
        backImageView = findViewById(R.id.scanning_rect2);
        messageContainer = findViewById(R.id.message_container);
        backImageView.setVisibility(View.GONE);
        removeDotLocation();

        handler = new Handler();
        messageHandler = null;


        options = new DMDSOptions();
        supportedDocumentList = getSupportedDocuments();
        options.setLicenseKey(LICENSE_KEY);

        DocTypeEnum initialDocument = DocTypeEnum.MoroccoFrontID;
        List<DocTypeEnum> documentTypes = new ArrayList<>();
        documentTypes.add(initialDocument);
        options.setDocumentTypes(documentTypes);



        try {
            dmdsBaseScanView.create(options, this);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // wait for view to be laid down and than initial
        dmdsBaseScanView.post(new Runnable() {
            @Override
            public void run() {
                initScanningView();
            }
        });

        initialDocTypes = options.getDocumentTypes();

        setMessageHeader(initialDocTypes.get(0));
        msgContent.setText(getString(R.string.start_scanning));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dmdsBaseScanView != null) {
                    btnScan.setVisibility(View.GONE);
                    dmdsBaseScanView.startScanning();
                }
            }
        });
        btnScanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dmdsBaseScanView != null) {
                    removeDotLocation();
                    backImageView.setVisibility(View.GONE);
                    scanningView.setBackground(ContextCompat.getDrawable(CustomScanActivity.this, R.drawable.scan_rect));
                    scanningView.setImageBitmap(null);
                    dmdsBaseScanView.updateDocumentTypes(initialDocTypes);
                    dmdsBaseScanView.restartScanning();
                    btnScanAgain.setVisibility(View.INVISIBLE);
                    btnDetails.setVisibility(View.GONE);
                    btnScan.setVisibility(View.VISIBLE);
                    dmdsBaseScanView.setVisibility(View.VISIBLE);
                    setMessageHeader(initialDocTypes.get(0));
                    msgContent.setText(getString(R.string.start_scanning));
                }
            }
        });

        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ResultActivity.newStartIntent(CustomScanActivity.this));
                finish();
            }
        });

        btnScanAgain.setVisibility(View.INVISIBLE);
        btnDetails.setVisibility(View.INVISIBLE);
    }

    void startMessageHandler() {
        if (messageHandler == null) {
            msgContent.setText(getString(R.string.msg_scanning_1));
            messageHandler = new Handler();
            messageHandler.postDelayed(infoMessage, 400);
        }
    }

    void initScanningView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightScreen = displayMetrics.heightPixels;
        widthScreen = displayMetrics.widthPixels;

        int[] scanRectLocation = new int[2];
        int[] rootLoc = new int[2];

        dmdsBaseScanView.getLocationOnScreen(rootLoc);
        scanningView.getLocationInWindow(scanRectLocation);

        scanRectLocation[0] -= rootLoc[0];
        scanRectLocation[1] -= rootLoc[1];
        float x = scanRectLocation[0] / (float) widthScreen;
        float y = scanRectLocation[1] / (float) heightScreen;
        float rectWidth = scanningView.getMeasuredWidth() / (float) widthScreen;
        float rectHeight = scanningView.getMeasuredHeight() / (float) heightScreen;

        dmdsBaseScanView.setScanningRegion(new DMDSRect(x, y, rectWidth, rectHeight), true);
    }

    void setMessageHeader(DocTypeEnum docTypeEnum) {
        setMessageHeader(docTypeEnum.name());
    }

    void setMessageHeader(String docType) {
        msgHeader.setText(String.format("Scanning: %s", docType));
    }

    void setDotLocation(DMDSQuadrangle dotLocation) {

        upperLeftDot.setVisibility(View.VISIBLE);
        upperLeftDot.setX(dotLocation.getUpperLeft().x);
        upperLeftDot.setY(dotLocation.getUpperLeft().y);

        upperRightDot.setVisibility(View.VISIBLE);
        upperRightDot.setX(dotLocation.getUpperRight().x);
        upperRightDot.setY(dotLocation.getUpperRight().y);

        lowerLeftDot.setVisibility(View.VISIBLE);
        lowerLeftDot.setX(dotLocation.getLowerLeft().x);
        lowerLeftDot.setY(dotLocation.getLowerLeft().y);

        lowerRightDot.setVisibility(View.VISIBLE);
        lowerRightDot.setX(dotLocation.getLowerRight().x);
        lowerRightDot.setY(dotLocation.getLowerRight().y);
    }

    void removeDotLocation() {
        upperLeftDot.setVisibility(View.GONE);
        upperRightDot.setVisibility(View.GONE);
        lowerLeftDot.setVisibility(View.GONE);
        lowerRightDot.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (dmdsBaseScanView != null) {
            dmdsBaseScanView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dmdsBaseScanView != null) {
            dmdsBaseScanView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dmdsBaseScanView != null) {
            dmdsBaseScanView.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dmdsBaseScanView != null) {
            dmdsBaseScanView.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dmdsBaseScanView != null) {
            dmdsBaseScanView.destroy();
        }
    }

    @Override
    public void mrzDetected(final DMDSMrzDetectionMetadata mrzMetadata) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                startMessageHandler();
            }
        });
    }

    @Override
    public void documentDetected(final DMDSDocumentDetectionMetadata documentDetected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                startMessageHandler();
                setDotLocation(documentDetected.getDetectionLocation());
            }
        });
    }

    @Override
    public void detectionTimeOut(final DMDSError dmdsError) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                scanningView.setBackground(ContextCompat.getDrawable(CustomScanActivity.this, R.drawable.scan_rect_error));
                setMessageHeader("Scanning Failed");
                msgContent.setText(dmdsError.description);
                btnScanAgain.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void documentScanned(final DMDSResult scanningResult) {
        if (scanningResult != null) {
            if (scanningResult.getDocument() != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        removeDotLocation();
                        setMessageHeader("Success!");

                        dmdsBaseScanView.setVisibility(View.INVISIBLE);
                        if (messageHandler != null) {
                            messageHandler.removeCallbacksAndMessages(null);
                            messageHandler = null;
                        }

                        frontImage = scanningResult.getDocument().getProcessedImage();
                        if (frontImage == null) {
                            frontImage = scanningResult.getDocument().getUnprocessedImage();
                        }
                        scanningView.setImageBitmap(frontImage);
                        scanningView.setBackground(null);
                        if (scanningResult.getDocument().getDocumentType().contentEquals(DocTypeEnum.EdgeDetection.toString())) {
                            msgContent.setText(getString(R.string.msg_edge_done));
                        } else {
                            msgContent.setText(String.format(getString(R.string.msg_scan_done), scanningResult.getDocument().getDocumentType()));
                        }
                        btnScanAgain.setVisibility(View.VISIBLE);
                        btnDetails.setVisibility(View.VISIBLE);
                        /*}*/
                    }
                });

                writeResults(scanningResult);
            }
        }
    }

    @Override
    public void documentScanFailed(DMDSError dmdsError) {
        AppLogger.e(TAG, dmdsError.description);

        if (dmdsError != null && !TextUtils.isEmpty(dmdsError.description)) {
            Toast.makeText(this, dmdsError.description, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void orientationChange(final DMDSOrientation orientation) {


    }

    @Override
    public void orientationAboutToChange(final DMDSOrientation currentOrientation, final DMDSOrientation nextOrientation) {

    }

    @Override
    public void fallingBackToEdgeDetection() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setMessageHeader(DocTypeEnum.EdgeDetection);
            }
        });
    }


    @Override
    public void notificationMessage(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(glareMsg)) {
                    msgContent.setText(msg);
                } else {
                    msgContent.setText(msg + "\n\n" + glareMsg);
                }
            }
        });
    }

    @Override
    public void glareDetected(final boolean hasGlare) {
        if (hasGlare) {
            glareMsg = getString(R.string.glare_detected);
        } else {
            glareMsg = "";
        }
    }

    public List<DocTypeEnum> getSupportedDocuments() {
        List<DocTypeEnum> retList = new ArrayList<>(Arrays.asList(DocTypeEnum.values()));
        retList.remove(DocTypeEnum.None); // Exclude "none" from scan option list
        Collections.sort(retList, new Comparator<DocTypeEnum>() {
            @Override
            public int compare(DocTypeEnum o1, DocTypeEnum o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        return retList;
    }

}
