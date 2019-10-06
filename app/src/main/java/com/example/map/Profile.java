package com.example.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.SyncTree;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {
    ImageView photo;
    private DatabaseReference mDatabaseRef;
    TextView mail1, text_name, text_name1, back, location, phone, blood, date, health;
    String currentUserID;
    FirebaseAuth firebaseAuth;
    String facebookUserId, username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        photo = findViewById(R.id.profilepic);
        blood = findViewById(R.id.text_blood);
        mail1 = findViewById(R.id.text_email1);
        date = findViewById(R.id.dob);
        back = findViewById(R.id.tv_back);
        phone = findViewById(R.id.text_phone);
        text_name = findViewById(R.id.text_name);
        text_name1 = findViewById(R.id.text_name1);
        health = findViewById(R.id.health_co);
        location = findViewById(R.id.text_location);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser().getPhoneNumber() != null){
            phone.setText(firebaseAuth.getCurrentUser().getPhoneNumber());
        }
        else{
            phone.setText("Click here to verify");
            phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Profile.this, PhoneActivity.class));
                }
            });
        }
        RetrieveUserInfo();
        for(UserInfo profile : firebaseAuth.getCurrentUser().getProviderData()) {
            if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                facebookUserId = profile.getUid();
                username = profile.getDisplayName();
                String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                Picasso.get().load(photoUrl).into(photo);
                text_name.setText(username);
                text_name1.setText(username);
            }
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void loadPic(){
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        storageReference.child(currentUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("UriImage",uri.toString());
                Picasso.get().load(uri).into(photo);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
    private void RetrieveUserInfo() {
        mail1.setText(firebaseAuth.getCurrentUser().getEmail());
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        mDatabaseRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveLocation = dataSnapshot.child("address").getValue().toString();
                    String retriveBlood = dataSnapshot.child("group").getValue().toString();
                    String retriveHealth = dataSnapshot.child("health_condition").getValue().toString();

                    if (!retrieveUserName.equals("") && !retrieveLocation.equals("")){
                        text_name.setText(retrieveUserName);
                        text_name1.setText(retrieveUserName);
                        health.setText(retriveHealth);
                        location.setText(retrieveLocation);
                        blood.setText(retriveBlood);
                    }
                    if(dataSnapshot.hasChild("dob")){
                        String dob = dataSnapshot.child("dob").getValue().toString();
                        date.setText(dob);
                    }
                    if(dataSnapshot.hasChild("image")) {
                        loadPic();
                    }
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    text_name.setText(retrieveUserName);
                    text_name1.setText(retrieveUserName);
                }
                else {
                    Toast.makeText(Profile.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
