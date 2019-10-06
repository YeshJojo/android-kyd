package com.example.map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class PhoneActivity extends AppCompatActivity {
    EditText otp_phone, verify_otp;
    Button otp_butt, verify_butt;
    ProgressDialog loading;
    FirebaseAuth firebaseAuth;
    String code, currentUserID, number;
    DatabaseReference mDatabaseRef;
    TextView textU, topText;
    private ConstraintLayout first, second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        otp_phone = findViewById(R.id.phonenumber);
        otp_butt = findViewById(R.id.otp_button);
        verify_otp = findViewById(R.id.verify_code);
        verify_butt = findViewById(R.id.verify_button);
        topText = findViewById(R.id.topText);

        first = findViewById(R.id.first_step);
        second = findViewById(R.id.secondStep);
        textU = findViewById(R.id.textView_noti);
        first.setVisibility(View.VISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        otp_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading = ProgressDialog.show(PhoneActivity.this, null, "Generating OTP...", true, true);
                sendVerificationCode();
            }
        });

        verify_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });
    }

    private void  verifyCode(){
        String otpCode = verify_otp.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(code, otpCode);
        signInWithPhoneAuthCredential(credential);

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null){
            firebaseAuth.updateCurrentUser(user).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(PhoneActivity.this, "Profile Updated.", Toast.LENGTH_LONG).show();
                        UpdateSettings();
                        startActivity(new Intent(PhoneActivity.this, MainActivity.class));
                    }
                    else {
                        if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                            Toast.makeText(PhoneActivity.this, "Incorrect Verification Code", Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });
        }
        user.updatePhoneNumber(credential).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(PhoneActivity.this, "Verification Sucessful", Toast.LENGTH_LONG).show();
                }
                else {
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(PhoneActivity.this, "Incorrect Verification Code", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    private boolean isValidMobile(String phone) {
        if (phone.length() < 11 || phone.length() > 13)
            return false;
        else
            return true;
    }
    private void sendVerificationCode() {
        String phoneNumber = otp_phone.getText().toString();
        if (phoneNumber.isEmpty()) {
            otp_phone.setError("Phone number is required");
            otp_phone.requestFocus();
            loading.dismiss();
        } else if (!isValidMobile(phoneNumber)){
            otp_phone.setError("Entered phone number is not valid");
            otp_phone.requestFocus();
            loading.dismiss();
        } else {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber, 60, TimeUnit.SECONDS, this, callbacks);
        }
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) { }

        @Override
        public void onVerificationFailed(FirebaseException e) { }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            code = s;
            loading.dismiss();
            otp_butt.setVisibility(View.GONE);
            verify_butt.setVisibility(View.VISIBLE);
            first.setVisibility(View.GONE);
            second.setVisibility(View.VISIBLE);
        }
    };
    private void UpdateSettings() {
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        number = otp_phone.getText().toString().trim();
        mDatabaseRef.child("Users").child(currentUserID).child("phone").setValue(number)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PhoneActivity.this, "Phone number updated successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(PhoneActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }
}
