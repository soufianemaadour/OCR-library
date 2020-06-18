package com.fawri.b3g.id.doc.result;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fawri.b3g.id.doc.R;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultViewHolder> {

    private final List<Result> resultList;

    ResultAdapter(List<Result> resultList) {
        this.resultList = resultList;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.li_result, null, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder resultViewHolder, int position) {
        resultViewHolder.bind(resultList.get(position));
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }
}
