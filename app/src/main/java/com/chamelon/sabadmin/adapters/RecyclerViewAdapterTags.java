package com.chamelon.sabadmin.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.chamelon.sabadmin.R;
import com.chamelon.sabadmin.info.Info;

import java.util.List;

public class RecyclerViewAdapterTags extends RecyclerView.Adapter<RecyclerViewAdapterTags.ViewHolderTags> implements Info {

    private List<String> tags;
    private Context context;
    private OnTagInteractionListener onTagInteractionListener;

    public interface OnTagInteractionListener{
        void onTagClicked(String tag);
    }

    public RecyclerViewAdapterTags(List<String> tags, Context context) {
        this.tags = tags;
        this.context = context;
        this.onTagInteractionListener = (OnTagInteractionListener) context;
    }

    @NonNull
    @Override
    public ViewHolderTags onCreateViewHolder(@NonNull ViewGroup v, int i) {
        View view = LayoutInflater.from(v.getContext())
                .inflate(R.layout.layout_tag_item, v, false);
        return new ViewHolderTags(view);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderTags h, int i) {

        try {
            h.tvTagName.setText(tags.get(h.getAdapterPosition()));
        } catch(ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    class ViewHolderTags extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tvTagName;

        ViewHolderTags(@NonNull View v) {
            super(v);
            tvTagName = v.findViewById(R.id.tv_tag_name);

            tvTagName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == tvTagName) {
                onTagInteractionListener.onTagClicked(tags.get(getAdapterPosition()));
            }
        }
    }
}
