package com.mygdx.game;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Dom on 20/02/2017.
 */

public class GameFragment extends AndroidFragmentApplication implements GoogleApiClient.OnConnectionFailedListener {
    LocationService locationService;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useGyroscope = false;
        locationService = new LocationServiceAndroid(this);
        return initializeForView(new MyGdxGame(locationService), config);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        log("LocationService", "Connection Failed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case R.integer.LOCATION_PERMISSION_REQUST : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationService.enable();
                } else {
                    locationService.disable();
                }
            }
        }
    }
}
