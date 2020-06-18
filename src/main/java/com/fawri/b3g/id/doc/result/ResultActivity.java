package com.fawri.b3g.id.doc.result;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daon.dmds.models.DMDSResult;
import com.fawri.b3g.id.doc.BaseActivity;
import com.fawri.b3g.id.doc.R;
import com.fawri.b3g.id.doc.verification.CaptureActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ResultActivity extends BaseActivity {

    private static final String ARG_RESULT = "arg_result";

    private RecyclerView rvResultList;
    private View vEmptyList;
    private ImageView ivFace;
    private ImageView ivDewarped;
    private ImageView ivFullFrame;
    private TextView tvProcessed;
    private TextView tvUnprocessed;
    private TextView tvFace;
    private Button  btnNext;

    public static Intent newStartIntent(Context context) {
        Intent intent = new Intent(context, ResultActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        rvResultList = findViewById(R.id.result_list);
        vEmptyList = findViewById(R.id.empty_list);
        ivFace = findViewById(R.id.iv_face);
        tvFace = findViewById(R.id.tv_face);
        ivDewarped = findViewById(R.id.iv_dewarped_doc);
        ivFullFrame = findViewById(R.id.iv_full_frame);
        tvProcessed = findViewById(R.id.tv_processed);
        tvUnprocessed = findViewById(R.id.tv_unprocessed);
        btnNext = findViewById(R.id.btnNext);


        populateList();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(CaptureActivity.class);
            }
        });
    }

    private void populateList() {
        DMDSResult result = readResults();

        if (result == null) {
            return;
        }

        if (result.getFace() != null && result.getFace().getFaceImage() != null) {
            ivFace.setVisibility(View.VISIBLE);
            ivFace.setImageBitmap(result.getFace().getFaceImage());
            tvFace.setVisibility(View.VISIBLE);
            tvFace.setText(getString(R.string.face_image, result.getFace().getFaceImage().getWidth() + "x" + result.getFace().getFaceImage().getHeight()));
        } else {
            ivFace.setVisibility(View.GONE);
            tvFace.setVisibility(View.GONE);
        }

        if (result.getDocument() == null) {
            return;
        }

        Bitmap processedBmp = result.getDocument().getProcessedImage();
        if (processedBmp != null) {
            ivDewarped.setVisibility(View.VISIBLE);
            ivDewarped.setImageBitmap(processedBmp);
            tvProcessed.setVisibility(View.VISIBLE);
            tvProcessed.setText(getString(R.string.dewarped_image, processedBmp.getWidth() + "x" + processedBmp.getHeight()));
        } else {
            ivDewarped.setVisibility(View.GONE);
            tvProcessed.setVisibility(View.GONE);
        }

        Bitmap unprocessedBmp = result.getDocument().getUnprocessedImage();
        if (unprocessedBmp != null) {
            ivFullFrame.setVisibility(View.VISIBLE);
            ivFullFrame.setImageBitmap(unprocessedBmp);
            tvUnprocessed.setVisibility(View.VISIBLE);
            tvUnprocessed.setText(getString(R.string.full_frame_image, unprocessedBmp.getWidth() + "x" + unprocessedBmp.getHeight()));
        } else {
            ivFullFrame.setVisibility(View.GONE);
            tvUnprocessed.setVisibility(View.GONE);
        }

        if (result.getDocument().getTextExtracted() != null) {
            if (result.getDocument().getTextExtracted() != null && !result.getDocument().getTextExtracted().isEmpty()) {
                rvResultList.setVisibility(View.VISIBLE);
                vEmptyList.setVisibility(View.GONE);
                btnNext.setVisibility(View.VISIBLE);

                Map<String, String> documentMap = result.getDocument().getTextExtracted();
                List<Result> resultList = new ArrayList<>();
                for (String key : documentMap.keySet()) {
                    resultList.add(new Result(key, documentMap.get(key)));
                }

                Collections.sort(resultList, new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                });

                ResultAdapter adapter = new ResultAdapter(resultList);
                rvResultList.setAdapter(adapter);
            } else {
                rvResultList.setVisibility(View.GONE);
                vEmptyList.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.GONE);
            }
        }
    }

    private void startActivity(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }
}
