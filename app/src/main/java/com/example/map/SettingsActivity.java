package com.example.map;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    private Switch share;
    RelativeLayout layout;
    TextView editPro, name, logout, changePw;
    ImageView photo;
    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        layout = findViewById(R.id.editLayout);
        editPro = findViewById(R.id.editProfile);
        photo = findViewById(R.id.profileCircleImageView);
        name = findViewById(R.id.usernameTextView);
        share = findViewById(R.id.shareLocation);
        logout = findViewById(R.id.settingsLogout);
        changePw = findViewById(R.id.changePassword);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Toast.makeText(SettingsActivity.this,"Login to continue.",Toast.LENGTH_LONG).show();
            finish();
            startActivity(new Intent(SettingsActivity.this,MainActivity.class));
        }
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, Profile.class));
            }
        });
        editPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, DonorActivity.class));
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            }
        });
        changePw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ForgotPassword.class));
            }
        });
        RetrieveUserInfo();
        share.setChecked(new ShareLocationPrefManager(this).isChecked());
        share.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ShareLocationPrefManager shareLocationPrefManager = new ShareLocationPrefManager(SettingsActivity.this);
                shareLocationPrefManager.shareLocation(!shareLocationPrefManager.isChecked());
                if(shareLocationPrefManager.isChecked()){
                    final int state =1;
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage("Do you want to share your live location with your contacts ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    UpdateSettings(state);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                if(!shareLocationPrefManager.isChecked()){
                    final int state =0;
                    UpdateSettings(state);
                }
            }
        });
    }
    private void UpdateSettings(int state) {
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("locationStatus", state);
        mDatabaseRef.child("Users").child(currentUserID).updateChildren(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Live location turned ON", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void loadPic() {
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        storageReference.child(currentUserID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("UriImage", uri.toString());
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
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        mDatabaseRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    if (!retrieveUserName.equals("")) {
                        name.setText(retrieveUserName);
                        if (dataSnapshot.hasChild("image")) {
                            loadPic();
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
