package com.fawri.b3g.id.doc.result;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.fawri.b3g.id.doc.R;

public class ResultViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvKey;
    private final TextView tvValue;

    ResultViewHolder(@NonNull View itemView) {
        super(itemView);
        tvKey = itemView.findViewById(R.id.tv_key);
        tvValue = itemView.findViewById(R.id.tv_value);
    }

    void bind(Result result) {
        tvKey.setText(result.getKey());
        tvValue.setText(result.getValue());
    }
}