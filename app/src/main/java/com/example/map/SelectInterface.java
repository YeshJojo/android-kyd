package com.example.map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SelectInterface extends AppCompatActivity {
    CardView gfit, mifit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_interface);

        gfit = findViewById(R.id.gfit_click);
        mifit = findViewById(R.id.mi_click);

        gfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectInterface.this, GfitStepCounter.class));
            }
        });
        mifit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectInterface.this, MiStepCounter.class));
            }
        });
    }
}
