package com.example.map;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.View;

import static android.content.Context.LOCATION_SERVICE;

public class LocationDialog {
    private Context context;

    public LocationDialog(){

    }
    public LocationDialog(Context context){
        this.context = context;
    }

    public void showNoInternetDialog(){
        final Dialog dialog1 = new Dialog(context, R.style.df_dialog);
        dialog1.setContentView(R.layout.dialog_no_location);
        dialog1.setCancelable(true);
        dialog1.setCanceledOnTouchOutside(true);
        dialog1.findViewById(R.id.btnSpinAndWinRedeem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
                dialog1.getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

            }
        });
        dialog1.show();
    }
    public boolean getLocationStatus() {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean isConnected = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isConnected) {
            showNoInternetDialog();
        }
        return isConnected;
    }

}
