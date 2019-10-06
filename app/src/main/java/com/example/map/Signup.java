package com.example.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class Signup extends AppCompatActivity {
    EditText mail, pwd;
    CircularProgressButton signup;
    ProgressDialog loading;
    FirebaseAuth firebaseAuth;
    TextView login;
    ImageView fb, goo;
    CallbackManager mCallbackManager;
    GoogleSignInClient googleSignInClient;
    GoogleApiClient googleApiClient;
    private DatabaseReference UsersRef;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mail = findViewById(R.id.login_email);
        pwd = findViewById(R.id.login_password);
        signup = findViewById(R.id.signup_button);
        login = findViewById(R.id.text_view_login);
        fb = findViewById(R.id.fb_login);
        goo = findViewById(R.id.google_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Signup.this, MainActivity.class));
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_key))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(Signup.this, "API failed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);

        goo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mail.getText().toString().isEmpty()) {
                    Toast.makeText(Signup.this, "Fill the empty fields", Toast.LENGTH_LONG).show();
                } else if (pwd.getText().toString().isEmpty()) {
                    Toast.makeText(Signup.this, "Fill the empty fields", Toast.LENGTH_LONG).show();
                }
                else {
                    signup.startAnimation();
                    firebaseAuth.createUserWithEmailAndPassword(mail.getText().toString(), pwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String currentUserId = firebaseAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                UsersRef.child(currentUserId).child("device_token")
                                        .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) { if (task.isSuccessful()) {
                                        Toast.makeText(Signup.this, "Registered Sucessfully.", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(Signup.this, DonorActivity.class));
                                        signup.revertAnimation();
                                        finish();
                                    } else {
                                        Toast.makeText(Signup.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        signup.revertAnimation();
                                    } }
                                });
                            } else {
                                Toast.makeText(Signup.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                signup.revertAnimation();
                            }
                        }
                    });
                }
            }
        });
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(Signup.this, Arrays.asList("email","public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }
                    @Override
                    public void onCancel() { }
                    @Override
                    public void onError(FacebookException error) { }
                });
            }
        });
    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, 101);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(Signup.this, "API connection failed.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Signup.this, "Registered Sucessfully. Please verify your mail ID", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Signup.this,PhoneActivity.class));
                            finish();
                        } else {
                            Toast.makeText(Signup.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if(firebaseAuth.getCurrentUser() != null){
                        email = firebaseAuth.getCurrentUser().getEmail();
                        firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                                if (isNewUser) {
                                    startActivity(new Intent(Signup.this, DonorActivity.class));
                                } else {
                                    startActivity(new Intent(Signup.this, HomeActivity.class));
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(Signup.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}