package com.example.map;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

public class DataUploadService extends IntentService {
    GfitStepCounter counter;
    private static final String TAG = DataUploadService.class.getSimpleName();

    public DataUploadService() {
        super(TAG );
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        counter.readData();
    }
}
