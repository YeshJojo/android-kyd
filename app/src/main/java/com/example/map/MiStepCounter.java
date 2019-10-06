package com.example.map;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.betomaluje.miband.ActionCallback;
import com.betomaluje.miband.MiBand;
import com.betomaluje.miband.model.BatteryInfo;

public class MiStepCounter extends AppCompatActivity {
    String TAG = "MiStepCounter";
    MiBand miBand;
    TextView connect;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_stepcounter);
        connect = findViewById(R.id.connect);
        miBand = MiBand.getInstance(MiStepCounter.this);
        loadingBar = ProgressDialog.show(MiStepCounter.this, null, "Searching for Mi device to connect...", true, false);
        if (!miBand.isConnected()) {
            miBand.connect(new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    Log.d(TAG, "Connected with Mi Band!");

                    BatteryInfo();
                }

                @Override
                public void onFail(int errorCode, String msg) {
                    Log.d(TAG, "Connection failed: " + msg);
                    loadingBar.dismiss();
                    connect.setText("Connection failed. " + msg);
                }
            });
        } else {
            loadingBar.dismiss();
            miBand.disconnect();
            connect.setText("Disconnecting Mi Band...");
        }
    }
    public void BatteryInfo(){
        miBand.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(final Object data) {
                BatteryInfo battery = (BatteryInfo) data;
                connect.setText("Battery: " + battery.toString());
            }
            @Override
            public void onFail(int errorCode, String msg) {
                Log.e(TAG, "Fail battery: " + msg);
            }
        });
    }
}
