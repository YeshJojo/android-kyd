package com.example.map.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.map.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramijemli.percentagechartview.PercentageChartView;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class Tuesday extends Fragment {

    public static final String BAR_STATE_ARG = "ProgressSubFragment.BAR_STATE_ARG";

    String currentUserID;
    String steps, dist, cal;
    float progress;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @BindView(R.id.stepCountText)
    TextView scText;
    @BindView(R.id.calCountText)
    TextView calText;
    @BindView(R.id.distanceCountText)
    TextView dcText;
    @BindView(R.id.day_label)
    TextView dayText;
    @BindView(R.id.chart)
    PercentageChartView mChart;
    @BindView(R.id.progress)
    TextView progress_tv;

    private Unbinder unbinder;
    private boolean enableBar;

    public Tuesday() {
    }

    public static Tuesday newInstance() {
        return new Tuesday();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.day_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RetrieveUserInfo();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    private void RetrieveUserInfo() {
        dayText.setText("Tuesday");
        if (auth.getCurrentUser() != null) {
            currentUserID = auth.getCurrentUser().getUid();
            reference.child("Users").child(currentUserID).child("fitData").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            if (dataSnapshot.hasChild("tuesday")){
                                steps = dataSnapshot.child("tuesday").child("steps").getValue().toString();
                                dist = dataSnapshot.child("tuesday").child("distance").getValue().toString();
                                cal = dataSnapshot.child("tuesday").child("cal").getValue().toString();
                                scText.setText(steps+" steps");
                                dcText.setText(dist+" KMs");
                                calText.setText(cal+" Cal");
                                progress = Float.parseFloat(steps)/10000*100;
                                DecimalFormat format = new DecimalFormat("#.0");
                                progress_tv.setText(String.valueOf(format.format(progress)));
                                mChart.setProgress(progress, false);
                            } else {
                                scText.setText("0");
                                dcText.setText("0.0Km");
                                calText.setText("0");
                            }
                        } catch (NullPointerException e) {
                            Log.d("null", e.getMessage());
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }
    }
}
