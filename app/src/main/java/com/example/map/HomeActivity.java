package com.example.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity implements  OnMapReadyCallback, LocationListener,GoogleMap.OnMarkerClickListener{
    private GoogleMap mMap;
    DatabaseReference mDatabaseRef;
    Marker marker;
    FirebaseAuth auth;
    TextView navUsername, navUsermail, navProfile, navLogout, navReg, navChat, navStep;
    ImageView profilePic, donoricon;
    String currentUserID, retrieveUserName, retrieveUserPic, retrieveLocationState;
    String addr, username, phone;
    String facebookUserId, googleUserId;
    Location current;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    ArrayList<com.example.map.UserInfo> info;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseApp.initializeApp(HomeActivity.this);
        auth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        if(auth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        handler = new Handler();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.username);
        navUsermail = headerView.findViewById(R.id.usermail);
        navProfile = headerView.findViewById(R.id.nav_profile);
        navLogout = headerView.findViewById(R.id.nav_logout);
        navReg = headerView.findViewById(R.id.nav_donor);
        navChat = headerView.findViewById(R.id.nav_chat);
        navStep = headerView.findViewById(R.id.nav_step);
        donoricon = headerView.findViewById(R.id.donoricon);
        profilePic = headerView.findViewById(R.id.userProfile);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLastLocation();
            }
        });

        if(auth.getCurrentUser()!=null) {
            for (UserInfo profile : auth.getCurrentUser().getProviderData()) {
                if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    facebookUserId = profile.getUid();
                    username = profile.getDisplayName();
                    String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                    Picasso.get().load(photoUrl).into(profilePic);
                    navUsername.setText(username);
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        } else {
            Task<Location> ta = fusedLocationProviderClient.getLastLocation();
            ta.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        Toast.makeText(HomeActivity.this, "Unable to load location", Toast.LENGTH_LONG).show();
                    }
                    if (location != null) {
                        current = location;
                        Log.d("Log-Lat", current.getLatitude() + "-" + current.getLongitude());
                    }
                }
            });
        }
        RetrieveUserInfo();

        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            }
        });
        navReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, DonorActivity.class));
            }
        });
        navChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ContactActivity.class));
            }
        });
        navStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, SelectInterface.class));
            }
        });
        navLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Toast.makeText( HomeActivity.this, "Logged Out",Toast.LENGTH_LONG).show();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapid);
        mapFragment.getMapAsync(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseRef.push().setValue(marker);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finishAffinity();
        }
    }
    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    current = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapid);
                    supportMapFragment.getMapAsync(HomeActivity.this);
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(current != null) {
            RetrieveUserInfo();
            LatLng latLng = new LatLng(current.getLatitude(), current.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Location");
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,25));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

        }
        mMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if(current!=null) {
            LatLng latLng = new LatLng(current.getLatitude(), current.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    donor user = s.getValue(donor.class);
                    LatLng location=new LatLng(user.latitude,user.longitude);
                    info = new ArrayList<>();
                    Marker mark = mMap.addMarker(new MarkerOptions().position(location).title(user.name+" - "+user.group));
                    if(user.group!=null){
                        if(user.group.contains("O"))
                            mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bloodo));
                        else if(user.group.equals("A+") || user.group.equals("A-"))
                            mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.blooda));
                        else if(user.group.equals("B+") || user.group.equals("B-"))
                            mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bloodb));
                        else if(user.group.contains("AB"))
                            mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bloodab));
                        else
                            mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dicon));
                    } else
                        mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.dicon));
                    info.add(new com.example.map.UserInfo(mark.getId(), user.name,user.address,user.phone, user.group));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                View v = getLayoutInflater().inflate(R.layout.contact, null);
                Button call = v.findViewById(R.id.call);
                Button msg = v.findViewById(R.id.msg);
                TextView contact = v.findViewById(R.id.contact_name);
                TextView address = v.findViewById(R.id.address);
                TextView group = v.findViewById(R.id.contact_group);
                for (int i = 0; i < info.size(); i++) {
                    if (marker.getId().equals(info.get(i).getId())) {
                        phone = info.get(i).getPhone();
                        contact.setText(info.get(i).getName());
                        group.setText("Blood Group: "+info.get(i).getGroup());
                        address.setText(info.get(i).getAddress());
                    }
                }
                call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        String p = "tel:" + phone;
                        intent.setData(Uri.parse(p));
                        startActivity(intent);
                    }
                });
                msg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(HomeActivity.this, ContactActivity.class));
                    }
                });
                builder.setView(v);
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) { }

    public void onStatusChanged(String provider, int status, Bundle extras) { }

    public void onProviderEnabled(String provider) { }

    public void onProviderDisabled(String provider) { }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void RetrieveUserInfo() {
        if (auth.getCurrentUser()!=null) {
            currentUserID = auth.getCurrentUser().getUid();
            navUsermail.setText(auth.getCurrentUser().getEmail());
            mDatabaseRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                        retrieveUserName = dataSnapshot.child("name").getValue().toString();
                        retrieveUserPic = dataSnapshot.child("image").getValue().toString();
                        navUsername.setText(retrieveUserName);
                        loadPic();
                    }
                    if((dataSnapshot.exists()) && (dataSnapshot.hasChild("locationStatus"))){
                        retrieveLocationState = dataSnapshot.child("locationStatus").getValue().toString();
                        UpdateLocation(retrieveLocationState);
                    }
                    try{
                        String donor = dataSnapshot.child("donor").getValue().toString();
                        if(donor.equals("1")){
                            navReg.setVisibility(View.GONE);
                            donoricon.setVisibility(View.GONE);
                        } else{
                            Toast.makeText(HomeActivity.this,"Register as Donor to Continue",Toast.LENGTH_LONG).show();
                        }
                    } catch (NullPointerException e){
                        Log.d("exception",e.getMessage());
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.msg_layout) {
            startActivity(new Intent(HomeActivity.this, ContactActivity.class));
        }
        return true;
    }
    public void loadPic(){
        currentUserID = auth.getCurrentUser().getUid();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        storageReference.child(currentUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d("UriImage",uri.toString());
                Picasso.get().load(uri).into(profilePic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
    public void UpdateLocation(String state){
        if(current!=null){
            if(state.equals("1")){
                HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("latitude", current.getLatitude());
                profileMap.put("longitude", current.getLongitude());
                mDatabaseRef.child(currentUserID).updateChildren(profileMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("locationState","Live location updated");
                                }
                                else {
                                    String message = task.getException().toString();
                                    Toast.makeText(HomeActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                fetchLastLocation();
            } finally {
                handler.postDelayed(mStatusChecker, 500);
            }
        }
    };
    void startRepeatingTask() {
        mStatusChecker.run();
    }
}