package com.example.map;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GfitStepCounter extends AppCompatActivity {
    TextView step, distance, calories, bpmTv;
    private Handler mHandler;
    long total;
    public static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    DatabaseReference mDatabaseRef;
    FirebaseAuth auth;
    String currentUserID;
    String day, totalSteps, totalDistance, totalCal, totalBpm;
    String monData,tueData,wedData,thuData,friData,satData,sunData;
    int monD,tueD,wedD,thuD,friD,satD,sunD;
    BarChart mBarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);
        step = findViewById(R.id.stepTrack);
        distance = findViewById(R.id.totalStepTrack);
        calories = findViewById(R.id.stepCal);
        bpmTv = findViewById(R.id.totalBpm);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        mHandler = new Handler();
        startRepeatingTask();

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.TYPE_HEART_RATE_BPM)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }
        mBarChart = findViewById(R.id.barchart);
        mBarChart.startAnimation();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                readData();
            }
        });
        RetrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
            }
        }
    }
    public void subscribe() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    private void readData() {
        try{
            Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    total =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    step.setText(String.valueOf(total));
                                    totalSteps = String.valueOf(total);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "There was a problem getting the step count.", e);
                                }
                            });
            Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    float kms =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                                    DecimalFormat format = new DecimalFormat("#.0");
                                    float kmMeter = kms/1000;
                                    distance.setText(String.valueOf(format.format(kmMeter)));
                                    totalDistance = String.valueOf(format.format(kmMeter));
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "There was a problem getting the step count.", e);
                                }
                            });
            Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    float bpm =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_BPM).asFloat();
                                    bpmTv.setText(String.valueOf(bpm));
                                    totalBpm = String.valueOf(bpm);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "There was a problem getting the step count.", e);
                                }
                            });
            Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    float cal =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                                    DecimalFormat format = new DecimalFormat("#.0");
                                    calories.setText(String.valueOf(format.format(cal)));
                                    totalCal = String.valueOf(format.format(cal));
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "There was a problem getting the step count.", e);
                                }
                            });


            Calendar calendar = Calendar.getInstance();
            day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).toLowerCase();

            currentUserID = auth.getCurrentUser().getUid();
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("steps", totalSteps);
            profileMap.put("distance", totalDistance);
            profileMap.put("cal", totalCal);
            profileMap.put("bpm", totalBpm);
            mDatabaseRef.child("Users").child(currentUserID).child("fitData").child(day).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d("DataUpdate","Data successfully added to firebase");
                            } else{
                                Log.d("DataUpdate","Task failed to add data to firebase");
                            }
                        }
                    });
        } catch (RuntimeException e){
            Log.d("Permission","Permission failed");
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                readData();
            } finally {
                mHandler.postDelayed(mStatusChecker, 500);
            }
        }
    };
    void startRepeatingTask() {
        mStatusChecker.run();
    }
    private void RetrieveUserInfo() {
        if (auth.getCurrentUser() != null) {
            currentUserID = auth.getCurrentUser().getUid();
            mDatabaseRef.child("Users").child(currentUserID).child("fitData").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            if (dataSnapshot.hasChild("monday"))
                                monData = dataSnapshot.child("monday").child("steps").getValue().toString();
                            if (dataSnapshot.hasChild("tuesday"))
                                tueData = dataSnapshot.child("tuesday").child("steps").getValue().toString();
                            if (dataSnapshot.hasChild("wednesday"))
                                wedData = dataSnapshot.child("wednesday").child("steps").getValue().toString();
                            if (dataSnapshot.hasChild("friday"))
                                friData = dataSnapshot.child("friday").child("steps").getValue().toString();
                            if (dataSnapshot.hasChild("saturday"))
                                satData = dataSnapshot.child("saturday").child("steps").getValue().toString();
                            if (dataSnapshot.hasChild("sunday"))
                                sunData = dataSnapshot.child("sunday").child("steps").getValue().toString();
                            if (dataSnapshot.hasChild("thursday"))
                                thuData = dataSnapshot.child("thursday").child("steps").getValue().toString();

                            if(!monData.isEmpty())
                                mBarChart.addBar(new BarModel("Mon",Integer.parseInt(monData),  0xFF18a3fe,0xFF18a3fe));
                            if(!tueData.isEmpty())
                                mBarChart.addBar(new BarModel("Tue",Integer.parseInt(tueData), 0xFF18a3fe));
                            if(!wedData.isEmpty())
                                mBarChart.addBar(new BarModel("Wed",Integer.parseInt(wedData), 0xFF18a3fe));
                            if(!thuData.isEmpty())
                                mBarChart.addBar(new BarModel("Thu",Integer.parseInt(thuData), 0xFF18a3fe));
                            if(!friData.isEmpty())
                                mBarChart.addBar(new BarModel("Fri",Integer.parseInt(friData),  0xFF18a3fe));
                            if(!satData.isEmpty())
                                mBarChart.addBar(new BarModel("Sat",Integer.parseInt(satData),  0xFF18a3fe));
                            if(!sunData.isEmpty())
                                mBarChart.addBar(new BarModel("Sun",Integer.parseInt(sunData),  0xFF18a3fe));
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
