package com.example.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DonorActivity extends AppCompatActivity {
    private static final int GalleryPick = 1;
    Button register, pick;
    Uri mImageUri;
    EditText name, address, dob, wgth, health_con;
    ProgressDialog loading;
    FirebaseAuth firebaseAuth;
    StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    private ProgressDialog loadingBar;
    ImageView photo;
    String currentUserID;
    Spinner grp, height;
    TextView back;
    RelativeLayout layout;
    Calendar calendar;
    DatePickerDialog datePickerDialog;
    int year, month, dayOfMonth;
    double latitude, longitude;
    String facebookUserId, username, phonenumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donor_content);
        name = findViewById(R.id.login_name);
        address = findViewById(R.id.login_address);
        dob = findViewById(R.id.login_dob);
        wgth = findViewById(R.id.weight);
        health_con = findViewById(R.id.health_condition);
        register = findViewById(R.id.signup_button);
        photo = findViewById(R.id.profilepic);
        back = findViewById(R.id.tv_back);
        pick = findViewById(R.id.pick_location);
        register.setVisibility(View.GONE);

        grp = findViewById(R.id.login_blood);
        height = findViewById(R.id.height);
        calendar = Calendar.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        if(firebaseAuth.getCurrentUser()!=null) {
            currentUserID = firebaseAuth.getCurrentUser().getUid();
            for (UserInfo profile : firebaseAuth.getCurrentUser().getProviderData()) {
                if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    facebookUserId = profile.getUid();
                    username = profile.getDisplayName();
                    phonenumber = profile.getPhoneNumber();
                    String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                    Picasso.get().load(photoUrl).into(photo);
                }
            }
        }
        try{
            String DonorLat = getIntent().getExtras().get("DonorLat").toString();
            String DonotLong = getIntent().getExtras().get("DonotLong").toString();
            String DonotAddr = getIntent().getExtras().get("DonotAddr").toString();

            if(!DonorLat.isEmpty()){
                pick.setVisibility(View.GONE);
                register.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);
                wgth.setVisibility(View.VISIBLE);
                height.setVisibility(View.VISIBLE);
                health_con.setVisibility(View.VISIBLE);
                address.setText(DonotAddr);
                latitude = Double.valueOf(DonorLat);
                longitude = Double.valueOf(DonotLong);

                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String uname = prefs.getString("username", "No name defined");
                String udob = prefs.getString("userdob", "Dob not defined");
                int ugrp = prefs.getInt("usergrp", 0);
                name.setText(uname);
                dob.setText(udob);
                grp.setSelection(ugrp);
            }
        } catch (NullPointerException e) {
            Log.d("NullPointer", e.getMessage());
        }
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
        dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(DonorActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                dob.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(photo.getDrawable() == null)
                    Toast.makeText(DonorActivity.this, "Please Select Image", Toast.LENGTH_LONG).show();
                else {
                    loading = ProgressDialog.show(DonorActivity.this, null, "Registering as Donor...", true, true);
                    UpdateSettings();
                    loading.dismiss();
                }
            }
        });
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                editor.putString("username",name.getText().toString());
                editor.putString("userdob",dob.getText().toString());
                editor.putInt("usergrp",grp.getSelectedItemPosition());
                editor.apply();
                startActivity(new Intent(DonorActivity.this, MapDragActivity.class));
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void UpdateSettings() {
        loadingBar = ProgressDialog.show(DonorActivity.this, null, "Uploading Profile Picture...", true, false);
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        String setUserName = name.getText().toString().trim();
        String add = address.getText().toString().trim();
        String weight = wgth.getText().toString().trim();
        String health = health_con.getText().toString().trim();
        String date = dob.getText().toString().trim();

        String bg = grp.getSelectedItem().toString();
        String hgt = height.getSelectedItem().toString();
        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please fill the empty fields...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(add)) {
            Toast.makeText(this, "Please fill the empty fields...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(weight)) {
            Toast.makeText(this, "Please fill the empty fields...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(health)) {
            Toast.makeText(this, "Please fill the empty fields...", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("address", add);
            profileMap.put("dob", date);
            profileMap.put("weight", weight);
            profileMap.put("height", hgt);
            profileMap.put("group", bg);
            profileMap.put("health_condition", health);
            profileMap.put("latitude", latitude);
            profileMap.put("longitude", longitude);
            profileMap.put("mail", firebaseAuth.getCurrentUser().getEmail());
            profileMap.put("donor", 1);

            mDatabaseRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addFitData();
                                Toast.makeText(DonorActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                startActivity(new Intent(DonorActivity.this, PhoneActivity.class));
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(DonorActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    void addFitData(){
        String[] day = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("steps","0");
        profileMap.put("bpm","0");
        profileMap.put("cal","0");
        profileMap.put("distance","0");
        for(int i=0;i<day.length;i++){
            mDatabaseRef.child("Users").child(currentUserID).child("fitData").child(day[i]).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("fitData","updated");
                            }
                            else {
                                String message = task.getException().toString();
                                Log.d("fitData",message);
                            }
                        }
                    });
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        currentUserID =  firebaseAuth.getCurrentUser().getUid();
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(photo);
        }
        if (resultCode == RESULT_OK) {
            Uri resultUri = mImageUri;
            StorageReference filePath = mStorageRef.child(currentUserID + ".jpg");
            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        final String downloaedUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                        mDatabaseRef.child("Users").child(currentUserID).child("image").setValue(downloaedUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("PicStatus","Profile Picture Loaded Successfully");
                                        } else {
                                            String message = task.getException().toString();
                                            Toast.makeText(DonorActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(DonorActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}