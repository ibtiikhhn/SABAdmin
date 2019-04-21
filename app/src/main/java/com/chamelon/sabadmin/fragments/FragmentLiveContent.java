package com.chamelon.sabadmin.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chamelon.sabadmin.R;
import com.chamelon.sabadmin.activities.ActivityPublishContent;
import com.chamelon.sabadmin.adapters.RecyclerViewAdapterLiveContent;
import com.chamelon.sabadmin.info.Info;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentLiveContent extends Fragment implements Info {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;

    private RecyclerView rvLiveContent;
    private List<DataSnapshot> liveContentSnaps;
    private RecyclerViewAdapterLiveContent adapterLiveContent;

    private DatabaseReference mRootRef;

    public FragmentLiveContent() {
        // Required empty public constructor
    }


    public static FragmentLiveContent newInstance(String param1, String param2) {
        FragmentLiveContent fragment = new FragmentLiveContent();
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
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mRootRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_content, container, false);
        rvLiveContent = view.findViewById(R.id.rv_withdrawal_requests);

        liveContentSnaps = new ArrayList<>();

        adapterLiveContent =
                new RecyclerViewAdapterLiveContent
                        (liveContentSnaps, ActivityPublishContent.sActivityPublishContent);

        rvLiveContent.setAdapter(adapterLiveContent);
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

        mRootRef.child(KEY_CONTENT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                liveContentSnaps.clear();
                Iterable<DataSnapshot> d = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : d) {
                    liveContentSnaps.add(snapshot);
                }
                Collections.reverse(liveContentSnaps);
                adapterLiveContent.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
