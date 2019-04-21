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

import java.util.List;

public class RecyclerViewAdapterPendingContent extends RecyclerView.Adapter<RecyclerViewAdapterPendingContent.ViewHolder> {

    private List<DataSnapshot> contentSnaps;
    private AdminActionListener adminActionListener;

    public interface AdminActionListener {

        void onUploadPendingClicked(DataSnapshot dataSnapshotWithdrawalRequest, Button btn);

        void onDeletePending(DataSnapshot dataSnapshotWithdrawalRequest, Button btn);

        void onDeleteContent(DataSnapshot dataSnapshotAccountApprovalRequest, Button btn);

    }

    public RecyclerViewAdapterPendingContent(List<DataSnapshot> contentSnaps, AdminActionListener adminActionListener) {

        this.contentSnaps = contentSnaps;
        this.adminActionListener = adminActionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_single_pending_content, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder v, int i) {

        Content content = contentSnaps.get(i).getValue(Content.class);

        if (content != null) {

            v.tvContentTitle.setText(content.getTitle());

            String date = new java.text.SimpleDateFormat("MMM/dd/yyyy HH:mm").format(content.getToBePublishedOn());

            v.tvPublishedOn.setText("To be published : " + date);
            v.tvContentId.setText("Content Id : " + content.getContentId());
            v.tvContentSource.setText("Content Source : " + content.getSource());
            v.sdvThumbnail.setImageURI(Uri.parse(content.getThumbnailUrl()));

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
        private TextView tvContentId;
        private TextView tvPublishedOn;
        private TextView tvContentTitle;
        private TextView tvContentSource;
        private SimpleDraweeView sdvThumbnail;

        private Button btnPublishNow;
        private Button btnCancel;

        public ViewHolder(View v) {
            super(v);

            tvTags = v.findViewById(R.id.tv_tags);
            tvContentId = v.findViewById(R.id.tv_content_id);
            sdvThumbnail = v.findViewById(R.id.sdv_thumbnail);
            tvContentTitle = v.findViewById(R.id.tv_content_title);
            tvContentSource = v.findViewById(R.id.tv_content_source);
            tvPublishedOn = v.findViewById(R.id.tv_to_be_published_on);

            btnPublishNow = v.findViewById(R.id.btn_publish_now);
            btnCancel = v.findViewById(R.id.btn_cancel);

            btnPublishNow.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (view == btnPublishNow) {
                try {
                    adminActionListener.onUploadPendingClicked(contentSnaps.get(getAdapterPosition()), btnPublishNow);
                    btnPublishNow.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (view == btnCancel) {
                try {
                    adminActionListener.onDeletePending(contentSnaps.get(getAdapterPosition()), btnCancel);
                    btnCancel.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
