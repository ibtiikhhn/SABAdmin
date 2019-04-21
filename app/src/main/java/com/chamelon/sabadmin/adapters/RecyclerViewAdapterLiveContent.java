package com.chamelon.sabadmin.adapters;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.chamelon.sabadmin.R;
import com.chamelon.sabadmin.pojo.Content;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.DataSnapshot;

import java.text.DecimalFormat;
import java.util.List;

public class RecyclerViewAdapterLiveContent extends RecyclerView.Adapter<RecyclerViewAdapterLiveContent.ViewHolder> {

    private List<DataSnapshot> contentSnaps;
    private RecyclerViewAdapterPendingContent.AdminActionListener adminActionListener;

    public RecyclerViewAdapterLiveContent(List<DataSnapshot> contentSnaps, RecyclerViewAdapterPendingContent.AdminActionListener adminActionListener) {

        this.contentSnaps = contentSnaps;
        this.adminActionListener = adminActionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_single_live_content, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder v, int i) {

        Content content = contentSnaps.get(i).getValue(Content.class);

        if (content != null) {

            v.tvContentTitle.setText(content.getTitle());

            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            decimalFormat.setGroupingUsed(true);
            decimalFormat.setGroupingSize(3);

            String date = new java.text.SimpleDateFormat("MMM/dd/yyyy HH:mm aa").format(content.getPublishedOn());

            v.tvPublishedOn.setText("Published on : " + date);
            v.tvContentId.setText("Content Id : " + content.getContentId());
            v.sdvThumbnail.setImageURI(Uri.parse(content.getThumbnailUrl()));
            v.tvContentSource.setText("Content Source : " + content.getSource());
            v.tvAppViewCount.setText("Views in app : " + decimalFormat.format(content.getAppViewsCount()));
            v.tvHostViewCount.setText("Views in host : " + decimalFormat.format(content.getViewCount()));
            v.tvLikes.setText("Likes : " + decimalFormat.format(content.getLikes()));

            StringBuilder t = new StringBuilder();
            for (String tag : content.getTags()) {
                t.append("{").append(tag).append("} ");
            }
            v.tvTags.setText("Tags : " + t);
        }
    }

    @Override
    public int getItemCount() {
        return contentSnaps.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvTags;
        private TextView tvLikes;
        private TextView tvContentId;
        private TextView tvPublishedOn;
        private TextView tvContentTitle;
        private TextView tvAppViewCount;
        private TextView tvHostViewCount;
        private TextView tvContentSource;
        private SimpleDraweeView sdvThumbnail;

        private Button btnRemove;

        ViewHolder(View v) {
            super(v);

            tvTags = v.findViewById(R.id.tv_tags);
            tvLikes = v.findViewById(R.id.tv_likes);
            tvContentId = v.findViewById(R.id.tv_content_id);
            sdvThumbnail = v.findViewById(R.id.sdv_thumbnail);
            tvPublishedOn = v.findViewById(R.id.tv_published_on);
            tvContentTitle = v.findViewById(R.id.tv_content_title);
            tvAppViewCount = v.findViewById(R.id.tv_app_view_count);
            tvContentSource = v.findViewById(R.id.tv_content_source);
            tvHostViewCount = v.findViewById(R.id.tv_host_view_count);

            btnRemove = v.findViewById(R.id.btn_remove);
            btnRemove.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (view == btnRemove) {
                try {
                    adminActionListener.onDeleteContent(contentSnaps.get(getAdapterPosition()), btnRemove);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
