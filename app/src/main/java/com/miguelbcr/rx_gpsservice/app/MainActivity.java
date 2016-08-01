package com.miguelbcr.rx_gpsservice.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.miguelbcr.rx_gps_service.lib.RxLocation;
import com.miguelbcr.rx_gpsservice.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RxLocation.REQUEST_CHECK_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.location_settings_enabled, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.location_settings_canceled, Toast.LENGTH_LONG).show();
            }
        }
    }
}
