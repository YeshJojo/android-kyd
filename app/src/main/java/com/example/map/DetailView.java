package com.example.map;

import android.os.Bundle;
import android.text.Html;
import android.transition.Explode;
import android.util.Log;
import android.view.animation.OvershootInterpolator;

import com.example.map.fragment.Friday;
import com.example.map.fragment.Monday;
import com.example.map.fragment.Saturday;
import com.example.map.fragment.Sunday;
import com.example.map.fragment.Thursday;
import com.example.map.fragment.Tuesday;
import com.example.map.fragment.Wednesday;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DetailView extends AppCompatActivity  {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tabs)
    SmartTabLayout mTAbs;
    String currentUserID;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FragmentPagerItemAdapter adapter;
    private Unbinder unbinder;
    String monData,tueData,wedData,thuData,friData,satData,sunData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);
        unbinder = ButterKnife.bind(this);
        setupLayout();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    private void setupLayout() {
        Explode transition = new Explode();
        transition.setDuration(600);
        transition.setInterpolator(new OvershootInterpolator(1f));
        getWindow().setEnterTransition(transition);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        currentUserID = auth.getCurrentUser().getUid();

        reference.child("Users").child(currentUserID).child("fitData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        if(dataSnapshot.hasChild("monday"))
                            monData = dataSnapshot.child("monday").child("steps").getValue().toString();
                        if(dataSnapshot.hasChild("tuesday"))
                            tueData = dataSnapshot.child("tuesday").child("steps").getValue().toString();
                        if(dataSnapshot.hasChild("wednesday"))
                            wedData = dataSnapshot.child("wednesday").child("steps").getValue().toString();
                        if(dataSnapshot.hasChild("friday"))
                            friData = dataSnapshot.child("friday").child("steps").getValue().toString();
                        if(dataSnapshot.hasChild("saturday"))
                            satData = dataSnapshot.child("saturday").child("steps").getValue().toString();
                        if(dataSnapshot.hasChild("sunday"))
                            sunData = dataSnapshot.child("sunday").child("steps").getValue().toString();
                        if(dataSnapshot.hasChild("thursday"))
                            thuData = dataSnapshot.child("thursday").child("steps").getValue().toString();

                        adapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), FragmentPagerItems.with(DetailView.this)
                                .add(Html.fromHtml("<font color='#18a3fe'>Monday</font>"), Monday.class)
                                .add(Html.fromHtml("<font color='#18a3fe'>Tuesday</font>"), Tuesday.class)
                                .add(Html.fromHtml("<font color='#18a3fe'>Wednesday</font>"), Wednesday.class)
                                .add(Html.fromHtml("<font color='#18a3fe'>Thursday</font>"), Thursday.class)
                                .add(Html.fromHtml("<font color='#18a3fe'>Friday</font>"), Friday.class)
                                .add(Html.fromHtml("<font color='#18a3fe'>Saturday</font>"), Saturday.class)
                                .add(Html.fromHtml("<font color='#18a3fe'>Sunday</font>"), Sunday.class).create());

                        mViewPager.setAdapter(adapter);
                        mViewPager.setOffscreenPageLimit(7);
                        mTAbs.setViewPager(mViewPager);
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