package com.chamelon.sabadmin.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.chamelon.sabadmin.R;
import com.chamelon.sabadmin.adapters.RecyclerViewAdapterPendingContent;
import com.chamelon.sabadmin.adapters.RecyclerViewAdapterTags;
import com.chamelon.sabadmin.adapters.ViewPagerAdapterAdmin;
import com.chamelon.sabadmin.asynctasks.FirebaseSendMessage;
import com.chamelon.sabadmin.databasehelpers.MyDBHelper;
import com.chamelon.sabadmin.fragments.FragmentLiveContent;
import com.chamelon.sabadmin.fragments.FragmentPendingContent;
import com.chamelon.sabadmin.info.Info;
import com.chamelon.sabadmin.pojo.Content;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivityPublishContent
        extends AppCompatActivity
        implements RecyclerViewAdapterPendingContent.AdminActionListener, FragmentPendingContent.OnFragmentInteractionListener, FragmentLiveContent.OnFragmentInteractionListener, View.OnClickListener, Info, RecyclerViewAdapterTags.OnTagInteractionListener {

    public static ActivityPublishContent sActivityPublishContent;

    private ViewPager viewPagerAdmin;
    private SimpleDraweeView sdvThumb;
    private FloatingActionButton fabUploadContent;
    private FloatingActionButton fabSendNotification;

    private SweetAlertDialog mProgressDialog;

    private EditText etContentTitle,
            etContentDescription,
            etContentLink,
            etViewCount,
            etDelayHrs,
            etDelayMins,
            etThumbnailUrl;

    private RecyclerViewAdapterTags adapterTags;

    private AutoCompleteTextView etTags;
    private TextView tvTags;

    private DatePicker dpPublishDate;

    private MyDBHelper dbHelper;
    private DatabaseReference mRootRef;
    private RequestQueue requestQueue;

    private Dialog mDialogEnterLink;
    private Dialog mDialogUploadContent;

    private long viewCount;
    private int HH, mm, attemptCount;

    private String title;
    private String link;
    private String site;
    private String hostUrl;
    private String thumbnailUrl;
    private String description;

    private List<String> tags;
    private List<String> predictiveTags;
    private List<String> rvTagsList;

    private void checkIntent() {

        Intent intent = getIntent();

        if (intent == null || intent.getAction() == null)
            return;

        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            if (intent.getType().startsWith("text/")) {
                mProgressDialog.show();
                title = intent.getStringExtra(Intent.EXTRA_TEXT);

                enqueueRequest(title);

                setIntent(null);
            }
        }
    }

    private void setListeners() {

        fabUploadContent.setOnClickListener(this);
        fabSendNotification.setOnClickListener(this);

        mDialogUploadContent.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                predictiveTags = dbHelper.getTags();
                rvTagsList.addAll(predictiveTags);

                tags.clear();
                mProgressDialog.hide();

                etContentTitle.setText(title);
                etContentLink.setText(link);
                etContentDescription.setText(description);
                etThumbnailUrl.setText(thumbnailUrl);
                etViewCount.setText("" + viewCount);

                if (thumbnailUrl != null)
                    sdvThumb.setImageURI(Uri.parse(thumbnailUrl));

                Calendar rightNow = Calendar.getInstance();

                etDelayHrs.setText("" + rightNow.get(Calendar.HOUR_OF_DAY));
                etDelayMins.setText("" + rightNow.get(Calendar.MINUTE));
                tvTags.setText("{No Tags Added}");
            }
        });

        etTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                rvTagsList.clear();
                if (s.length() == 0) {
                    rvTagsList.addAll(predictiveTags);
                    adapterTags.notifyDataSetChanged();
                    return;
                }

                for (int i = 0; i < predictiveTags.size(); i++) {
                    if (predictiveTags.get(i).toLowerCase().contains(s.toString().toLowerCase())) {
                        rvTagsList.add(0, predictiveTags.get(i));
                    }
                }
                adapterTags.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initDialogs() {

        mProgressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mProgressDialog.setTitleText("Fetching Data...");
        mProgressDialog.setCancelable(false);

        mDialogEnterLink = new Dialog(this);
        mDialogUploadContent = new Dialog(this);

        mDialogUploadContent.setContentView(R.layout.dialog_upload_content);
        mDialogEnterLink.setContentView(R.layout.dialog_enter_link);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = mDialogUploadContent.getWindow();
        Window w = mDialogEnterLink.getWindow();
        lp.copyFrom(window.getAttributes());

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.horizontalMargin = 25f;
        lp.verticalMargin = 25f;
        window.setAttributes(lp);

        lp.copyFrom(w.getAttributes());

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.horizontalMargin = 25f;
        lp.verticalMargin = 25f;

        w.setAttributes(lp);

        mDialogEnterLink.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDialogUploadContent.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rvTags = mDialogUploadContent.findViewById(R.id.rv_tags);
        etDelayHrs = mDialogUploadContent.findViewById(R.id.et_hours);
        etContentLink = mDialogUploadContent.findViewById(R.id.et_link);
        etDelayMins = mDialogUploadContent.findViewById(R.id.et_minutes);
        sdvThumb = mDialogUploadContent.findViewById(R.id.sdv_thumbnail);
        etViewCount = mDialogUploadContent.findViewById(R.id.et_view_count);
        etThumbnailUrl = mDialogUploadContent.findViewById(R.id.et_thumb_url);
        dpPublishDate = mDialogUploadContent.findViewById(R.id.dp_publish_date);
        etContentTitle = mDialogUploadContent.findViewById(R.id.et_content_title);
        etContentDescription = mDialogUploadContent.findViewById(R.id.et_description);

        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        rvTags.setAdapter(adapterTags);
        rvTags.setLayoutManager(horizontalLayoutManager);

        dpPublishDate.setMinDate(System.currentTimeMillis());

        etTags = mDialogUploadContent.findViewById(R.id.et_tag);

        ImageButton ibAddTag = mDialogUploadContent.findViewById(R.id.ib_add_tag);
        ImageButton ibResetTags = mDialogUploadContent.findViewById(R.id.ib_reset_tags);
        ImageButton ibRefreshThumbnail = mDialogUploadContent.findViewById(R.id.ib_refresh_thumbnail);

        tvTags = mDialogUploadContent.findViewById(R.id.tv_tags);

        Button publishContent = mDialogUploadContent.findViewById(R.id.btn_publish_now);
        Button cancelDialog = mDialogUploadContent.findViewById(R.id.btn_cancel);
        Button pendContent = mDialogUploadContent.findViewById(R.id.btn_pend);

        final EditText etContentLink = mDialogEnterLink.findViewById(R.id.et_content_link);
        Button btnGetData = mDialogEnterLink.findViewById(R.id.btn_get_data);

        publishContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!areFieldCorrect()) {
                    return;
                }

                final Content content = new Content();
                content.setContentId(System.currentTimeMillis());
                content.setPublishedOn(System.currentTimeMillis());
                content.setTitle(title);
                content.setContentUrl(link);
                content.setDescription(description);
                content.setHostUrl(hostUrl);
                content.setSource(site);
                content.setViewCount(viewCount);
                content.setThumbnailUrl(thumbnailUrl);
                content.setTags((ArrayList<String>) tags);

                mRootRef.child(KEY_CONTENT)
                        .push()
                        .setValue(content)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                try {
                                    new FirebaseSendMessage(content.getContentId()).execute();
                                } catch (Exception e) {
                                    Log.d("Exception", e.toString());
                                }
                                Toast.makeText(ActivityPublishContent.this, "Content Added.", Toast.LENGTH_SHORT).show();
                                mDialogUploadContent.cancel();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ActivityPublishContent.this, "Failed to add content.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        pendContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!areFieldCorrect()) {
                    return;
                }

                HH = Integer.parseInt(etDelayHrs.getText().toString());
                mm = Integer.parseInt(etDelayMins.getText().toString());

                Content content = new Content();
                content.setContentId(System.currentTimeMillis());
                content.setToBePublishedOn(getDateFromDatePicker(dpPublishDate).getTime());
                content.setTitle(title);
                content.setContentUrl(link);
                content.setDescription(description);
                content.setSource(site);
                content.setViewCount(viewCount);
                content.setHostUrl(hostUrl);
                content.setThumbnailUrl(thumbnailUrl);
                content.setTags((ArrayList<String>) tags);

                mRootRef.child(KEY_PENDING_CONTENT)
                        .push()
                        .setValue(content)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ActivityPublishContent.this, "Content Added to pending.", Toast.LENGTH_SHORT).show();
                                mDialogUploadContent.cancel();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ActivityPublishContent.this, "Failed to add content.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialogUploadContent.cancel();
            }
        });

        ibRefreshThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etThumbnailUrl.getText() == null || etThumbnailUrl.getText().toString().length() == 0) {
                    Toast.makeText(ActivityPublishContent.this, "Failed to load thumbnail.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(ActivityPublishContent.this, "Loading Thumbnail.", Toast.LENGTH_SHORT).show();
                sdvThumb.setImageURI(Uri.parse(etThumbnailUrl.getText().toString()));
            }
        });

        ibAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onTagClicked(etTags.getText().toString());

            }
        });

        ibResetTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tags.clear();
                tvTags.setText("{No Tags added}");
            }
        });

        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etContentLink.getText() == null || etContentLink.getText().toString().length() == 0) {
                    Toast.makeText(ActivityPublishContent.this, "Enter a valid link to video.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String link = etContentLink.getText().toString();
                enqueueRequest(link);
                mProgressDialog.show();
            }
        });
    }

    private Date getDateFromDatePicker(DatePicker datePicker) {

        int day, month, year;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            day = datePicker.getDayOfMonth();
            month = datePicker.getMonth();
            year = datePicker.getYear();
        } else {
            day = datePicker.getDayOfMonth() + 1;
            month = datePicker.getMonth() + 1;
            year = datePicker.getYear();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, HH, mm);
        Log.v(TAG, "Time : " + calendar.getTime());
        return calendar.getTime();
    }

    private boolean areFieldCorrect() {

        if (etContentTitle.getText() == null || etContentTitle.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "Title Required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etViewCount.getText() == null || etViewCount.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "ViewCount Required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etThumbnailUrl.getText() == null || etThumbnailUrl.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "Thumbnail Url Required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etContentLink.getText() == null || etContentLink.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "Content link Required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etContentDescription.getText() == null || etContentDescription.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "Content description Required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDelayHrs.getText() == null || etDelayHrs.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "Enter Hours.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (Integer.parseInt(etDelayHrs.getText().toString()) < 0 || Integer.parseInt(etDelayHrs.getText().toString()) > 24) {
            Toast.makeText(sActivityPublishContent, "Enter Hours > 0 < 24", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etDelayMins.getText() == null || etDelayMins.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "Enter minutes.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (Integer.parseInt(etDelayMins.getText().toString()) < 0 || Integer.parseInt(etDelayMins.getText().toString()) > 60) {
            Toast.makeText(sActivityPublishContent, "Enter Hours > 0 < 60", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etContentDescription.getText() == null || etContentDescription.getText().toString().length() == 0) {
            Toast.makeText(sActivityPublishContent, "Content description Required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etThumbnailUrl.getText() == null || etThumbnailUrl.getText().length() == 0 || Uri.parse(etThumbnailUrl.getText().toString()) == null) {
            Toast.makeText(sActivityPublishContent, "Please, Enter a valid thumbnail url.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tags.size() < 1) {
            Toast.makeText(sActivityPublishContent, "Enter at least one tag", Toast.LENGTH_SHORT).show();
            return false;
        } else if (tags.size() > 5) {
            Toast.makeText(sActivityPublishContent, "More than five tags not allowed", Toast.LENGTH_SHORT).show();
            return false;
        }

        link = etContentLink.getText().toString();
        title = etContentTitle.getText().toString();
        thumbnailUrl = etThumbnailUrl.getText().toString();
        description = etContentDescription.getText().toString();
        viewCount = Long.parseLong(etViewCount.getText().toString());
        HH = Integer.parseInt(etDelayHrs.getText().toString());
        mm = Integer.parseInt(etDelayMins.getText().toString());

        return true;
    }

    private void initMembers() {

        sActivityPublishContent = this;
        dbHelper = new MyDBHelper(this);

        predictiveTags = new ArrayList<>();

        tags = new ArrayList<>();
        rvTagsList = new ArrayList<>();

        adapterTags = new RecyclerViewAdapterTags(rvTagsList, this);

        requestQueue = Volley.newRequestQueue(this);
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void initView() {

        viewPagerAdmin = findViewById(R.id.vp_admin);
        fabUploadContent = findViewById(R.id.fab_upload_activity);
        fabSendNotification = findViewById(R.id.fab_send_notifications);

        TabLayout tlAdmin = findViewById(R.id.tl_admin);

        tlAdmin.addTab(tlAdmin.newTab().setText("Pending Content"));
        tlAdmin.addTab(tlAdmin.newTab().setText("Live Content"));
        tlAdmin.setTabGravity(TabLayout.GRAVITY_FILL);

        ViewPagerAdapterAdmin viewPagerAdapterAdmin =
                new ViewPagerAdapterAdmin(getSupportFragmentManager(), 2);

        viewPagerAdmin.setAdapter(viewPagerAdapterAdmin);
        viewPagerAdmin.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tlAdmin));

        tlAdmin.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPagerAdmin.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void enqueueRequest(final String videoUrl) {

        mProgressDialog.show();
        final String requestLink = KEY_API_BASE_URL + "key=" + KEY_API_EMBED_API + "&url=" + videoUrl +
                "&autoplay=1";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, requestLink, null, new Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                if (response != null) try {

                    if (response.has("oembed")) {

                        mProgressDialog.hide();
                        site = response.getString("site");
                        title = response.getString("title");
                        hostUrl = response.getString("url");

                        if (!response.isNull("description")) {
                            description = response.getString("description");
                        } else {
                            description = "The Host provides no description.. Please enter a description";
                        }

                        response = response.getJSONObject("oembed");
                        thumbnailUrl = response.getString("thumbnail_url");

                        link = response.getString("html").replace("\\", "").replace("allowfullscreen", "allowfullscreen=\"0\"");
                        viewCount = (long) (Math.random() * 99999 + 10001);
                        mDialogUploadContent.show();

                    } else {

                        mProgressDialog.hide();
                        site = response.getString("site");
                        title = response.getString("title");
                        hostUrl = response.getString("url");

                        if (!response.isNull("description")) {
                            description = response.getString("description");
                        } else {
                            description = "The Host provides no description.. Please enter a description";
                        }

                        JSONArray images = response.getJSONArray("images");
                        JSONObject image = (JSONObject) images.get(0);
                        thumbnailUrl = image.getString("url");

                        JSONArray videos = response.getJSONArray("videos");

                        boolean flag = false;
                        for (int i = 0; i < videos.length(); i++) {
                            JSONObject o = (JSONObject) videos.get(i);
                            link = o.getString("url");
                            Log.v(TAG, o.toString());
                            if (o.getString("type").replace("\\", "").equals("video/mp4")) {
                                link = o.getString("url").replace("\\", "");
                                Log.v(TAG, "video/mp4 found");
                                flag = true;
                            }
                        }

                        if (flag) {
                            Toast.makeText(ActivityPublishContent.this, "The API does not provide data for this video.", Toast.LENGTH_SHORT).show();
                            mProgressDialog.hide();
                            return;
                        }

                        link = START_URL_WITH_IFRAME + link + END_URL_WITH_IFRAME;
                        Log.v(TAG, "Link : " + link);
                        viewCount = (long) (Math.random() * 99999 + 10001);
                        mDialogUploadContent.show();

                    }
                } catch (JSONException e) {
                    if (attemptCount < 3) {
                        enqueueRequest(requestLink);
                        attemptCount++;
                    } else {
                        Toast.makeText(ActivityPublishContent.this, "Failed to get data.", Toast.LENGTH_SHORT).show();
                        Log.v(TAG, "JSON not formatted correctly");
                        mProgressDialog.hide();
                    }
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        if (attemptCount < 3) {
                            enqueueRequest(requestLink);
                            attemptCount++;
                        } else {

                            Toast.makeText(ActivityPublishContent.this, "Failed to get data.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "error " + volleyError.toString());
                            mProgressDialog.hide();
                        }
                    }
                }) {
        };

        requestQueue.add(postRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_content);

        initView();
        initMembers();

        initDialogs();



        setListeners();

        checkIntent();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDialogUploadContent.dismiss();
        mProgressDialog.dismiss();
    }

    @Override
    public void onClick(View v) {

        if (fabUploadContent == v) {
            mDialogEnterLink.show();
        }

        if (fabSendNotification == v) {

            Log.i(TAG, "onClick: "+"fab clicked");
            final AlertDialog dialogBuilder = new AlertDialog.Builder(ActivityPublishContent.this).create();
            LayoutInflater inflater = ActivityPublishContent.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.notification_dialogue_view, null);

            final EditText titleET = dialogView.findViewById(R.id.titleET);
            final EditText messageET = dialogView.findViewById(R.id.messageET);
            Button sendNOT = dialogView.findViewById(R.id.sendNotificationBT);

            sendNOT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title = titleET.getText().toString().trim();
                    String message = messageET.getText().toString().trim();

                    if (title.isEmpty()) {
                        titleET.setError("This Can't be empty");
                        return;
                    }
                    if (message.isEmpty()) {
                        messageET.setError("This Can't be empty");
                    } else {
                        // send your message here
                        dialogBuilder.dismiss();
                    }

                }
            });
            dialogBuilder.setView(dialogView);
            dialogBuilder.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkIntent();
    }

    @Override
    public void onUploadPendingClicked(final DataSnapshot dataSnapshot, final Button btn) {

        Toast.makeText(sActivityPublishContent, "Publishing Content now.", Toast.LENGTH_SHORT).show();
        final Content c = dataSnapshot.getValue(Content.class);
        c.setPublishedOn(System.currentTimeMillis());

        mRootRef
                .child(KEY_CONTENT)
                .push()
                .setValue(c)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        try {
                            new FirebaseSendMessage(c.getContentId()).execute();
                        } catch (Exception e) {
                            Log.d("Exception", e.toString());
                        }
                        dataSnapshot.getRef().removeValue();
                        btn.setEnabled(true);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ActivityPublishContent.this, "An error occurred, check your connection and try again", Toast.LENGTH_SHORT).show();
                        btn.setEnabled(true);

                    }
                });
    }

    @Override
    public void onDeletePending(DataSnapshot dataSnapshot, Button btn) {

        dataSnapshot
                .getRef()
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(ActivityPublishContent.this, "Removed pending content.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDeleteContent(DataSnapshot dataSnapshot, Button btn) {

        dataSnapshot
                .getRef()
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(ActivityPublishContent.this, "Removed Live content.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void addTag(String tag) {

        if (tag == null || tag.length() == 0) {
            Toast.makeText(ActivityPublishContent.this, "Enter a Tag.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tags.size() == 5) {
            Toast.makeText(ActivityPublishContent.this, "More than five tags not allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tag.contains(" ")) {
            Toast.makeText(ActivityPublishContent.this, "No spaces allowed.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.isTagSaved(tag.trim())) {
            dbHelper.addTag(etTags.getText().toString().trim());
            Log.v(TAG, "Tag saved");
        }

        tags.add(tag);
        StringBuilder t = new StringBuilder();

        for (String t1 : tags) {
            t.append("{").append(t1).append("} ");
        }

        tvTags.setText(t);
        etTags.setText("");
    }

    @Override
    public void onTagClicked(String tag) {

        if (tags.contains(tag)) {
            Toast.makeText(this, "Tag already added.", Toast.LENGTH_SHORT).show();
            return;
        }

        addTag(tag);
        predictiveTags = dbHelper.getTags();
    }
}
