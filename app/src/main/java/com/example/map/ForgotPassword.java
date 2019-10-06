package com.example.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    EditText email;
    Button sendCode, homeRedirect;
    FirebaseAuth auth;
    LinearLayout first, second, load;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        email = findViewById(R.id.emailAddr);
        sendCode = findViewById(R.id.send_btn);
        homeRedirect = findViewById(R.id.gotoHome);
        first = findViewById(R.id.firstStep);
        second = findViewById(R.id.secondStep);
        load = findViewById(R.id.loadinglayout);
        auth = FirebaseAuth.getInstance();
        email.setText(auth.getCurrentUser().getEmail());
        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!email.getText().toString().equals("")){
                    first.setVisibility(View.GONE);
                    second.setVisibility(View.GONE);
                    sendCode.setVisibility(View.GONE);
                    load.setVisibility(View.VISIBLE);
                    auth.sendPasswordResetEmail(email.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                auth.signOut();
                                first.setVisibility(View.GONE);
                                second.setVisibility(View.VISIBLE);
                                sendCode.setVisibility(View.GONE);
                                homeRedirect.setVisibility(View.VISIBLE);
                            } else{
                                Toast.makeText(ForgotPassword.this, "Failed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else{
                    Toast.makeText(ForgotPassword.this, "Enter Email address",Toast.LENGTH_LONG).show();
                    first.setVisibility(View.VISIBLE);
                    second.setVisibility(View.VISIBLE);
                    sendCode.setVisibility(View.VISIBLE);
                    load.setVisibility(View.GONE);
                }
            }
        });
        homeRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(ForgotPassword.this,MainActivity.class));
            }
        });
    }
}
