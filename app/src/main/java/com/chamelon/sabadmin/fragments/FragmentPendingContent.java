package com.chamelon.sabadmin.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.chamelon.sabadmin.R;
import com.chamelon.sabadmin.activities.ActivityPublishContent;
import com.chamelon.sabadmin.adapters.RecyclerViewAdapterPendingContent;
import com.chamelon.sabadmin.info.Info;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentPendingContent extends Fragment implements Info {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private List<DataSnapshot> pendingContentSnaps;
    private RecyclerView rvPendingContent;

    private DatabaseReference mRootRef;
    private RecyclerViewAdapterPendingContent adapter;

    public FragmentPendingContent() {
        // Required empty public constructor
    }

    public static FragmentPendingContent newInstance(String param1, String param2) {
        FragmentPendingContent fragment = new FragmentPendingContent();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pending_content, container, false);
        rvPendingContent = view.findViewById(R.id.rv_account_requests);

        pendingContentSnaps = new ArrayList<>();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        adapter = new RecyclerViewAdapterPendingContent(pendingContentSnaps, ActivityPublishContent.sActivityPublishContent);
        rvPendingContent.setAdapter(adapter);

        setup();

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setup() {

        mRootRef.child(KEY_PENDING_CONTENT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                pendingContentSnaps.clear();
                Iterable<DataSnapshot> d = dataSnapshot.getChildren();

                for (DataSnapshot snapshot : d) {
                    pendingContentSnaps.add(snapshot);
                }

                Collections.reverse(pendingContentSnaps);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
