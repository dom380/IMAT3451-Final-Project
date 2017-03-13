package dmu.mygdx.game;

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
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration(); //Configure android application settings
        config.useAccelerometer = false;
        config.useGyroscope = false; //Disable to save power. Not needed.
        locationService = new LocationServiceAndroid(this); //Create Android location service and pass to our interface.
        return initializeForView(new MyGdxGame(locationService, getResources().getString(R.string.weather_api_url), getResources().getString(R.string.weather_api_key)), config); //Create the main game class.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        log("LocationService", "Connection Failed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case R.integer.LOCATION_PERMISSION_REQUEST : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationService.enable();
                } else {
                    locationService.disable();
                }
            }
        }
    }
}
