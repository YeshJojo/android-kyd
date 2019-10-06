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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class MainActivity extends AppCompatActivity {
    EditText email, password;
    private FirebaseAuth auth;
    ImageView fb, goo;
    TextView reg;
    CircularProgressButton login;
    CallbackManager mCallbackManager;
    GoogleSignInClient googleSignInClient;
    GoogleApiClient googleApiClient;
    String FbEmail="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCallbackManager = CallbackManager.Factory.create();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.cirLoginButton);

        fb = findViewById(R.id.facebookLogin);
        goo = findViewById(R.id.GooLogin);
        reg = findViewById(R.id.nav_register);
        FirebaseApp.initializeApp(MainActivity.this);
        auth = FirebaseAuth.getInstance();

        if(new LocationDialog(this).getLocationStatus()){
            if(auth.getCurrentUser() != null) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }
        }
        if(auth.getCurrentUser()!=null){
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_key))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "API failed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLogin();
            }
        });
        goo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Signup.class));
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new InternetDialog(MainActivity.this).getInternetStatus()) {
                    login.startAnimation();
                    loginFun();
                }
            }
        });

    }
    public void loginFun(){
        if (email.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Fill the empty fields", Toast.LENGTH_LONG).show();
        }
        else if(password.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Fill the empty fields", Toast.LENGTH_LONG).show();
        }
        else {
            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        password.setText("");
                        startActivity(new Intent(MainActivity.this,HomeActivity.class));
                        login.revertAnimation();
                    }else {
                        password.setText("");
                        login.revertAnimation();
                        Toast.makeText(MainActivity.this, "Login Failed. Enter correct password", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    public void fbLogin(){
        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email","public_profile"));
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
                Toast.makeText(MainActivity.this, "API connection failed.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Registered Sucessfully.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this,MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if(auth.getCurrentUser() != null){
                        FbEmail = auth.getCurrentUser().getEmail();
                        auth.fetchSignInMethodsForEmail(FbEmail).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                                if (isNewUser) {
                                    startActivity(new Intent(MainActivity.this, DonorActivity.class));
                                } else {
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}