package dmu.mygdx.game;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Dom on 20/02/2017.
 *
 * Implementation of LocationService using Google Play Services.
 */

public class LocationServiceAndroid implements LocationService, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GameFragment launcher;
    private GoogleApiClient mGoogleApiClient;
    private boolean isAvailable, connected = false, enabled;
    private Location lastLocation;

    /**
     * Constructor.
     *
     * @param activity Fragment Activity containing our game.
     */
    LocationServiceAndroid(GameFragment activity) {
        this.launcher = activity;
        enabled = true;
        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        int playServicesAvailable = instance.isGooglePlayServicesAvailable(launcher.getContext());
        if (playServicesAvailable == ConnectionResult.SUCCESS) { //If we have access to Google Play Services
            isAvailable = true;
            this.mGoogleApiClient = new GoogleApiClient.Builder(activity.getContext()) //Create a self managing client
                    .enableAutoManage(launcher.getActivity(), launcher)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            connect();
        } else {
            Dialog errorDialog = instance.getErrorDialog(launcher.getActivity(), playServicesAvailable, 1);
            errorDialog.show();
            isAvailable = false;
        }
    }

    @Override
    public void connect() {
        mGoogleApiClient.connect();
        connected = true;
    }

    @Override
    public void disconnect() {
        mGoogleApiClient.disconnect();
        connected = false;
    }

    @Override
    public boolean isAvailable() {
        return isAvailable && enabled;
    }

    @Override
    public double[] getLatLong() {
        if (!enabled) {
            return null;
        }
        if (lastLocation != null)
            return new double[]{lastLocation.getLatitude(), lastLocation.getLongitude()};
        else {
            //noinspection MissingPermission
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return lastLocation != null ? new double[]{lastLocation.getLatitude(), lastLocation.getLongitude()} : null;
        }

    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(30000) //30sec
                .setFastestInterval(10000); //19 sec
        if (ActivityCompat.checkSelfPermission(launcher.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(launcher.getContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(launcher.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(launcher.getActivity(), launcher.getString(R.string.location_explanation))) {
                //Check to see if we should explain the request
            } else {
                //Request permissions
                ActivityCompat.requestPermissions(launcher.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, launcher.getResources().getInteger(R.integer.LOCATION_PERMISSION_REQUEST));
                ActivityCompat.requestPermissions(launcher.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, launcher.getResources().getInteger(R.integer.LOCATION_PERMISSION_REQUEST));
                ActivityCompat.requestPermissions(launcher.getActivity(), new String[]{Manifest.permission.INTERNET}, launcher.getResources().getInteger(R.integer.INTERNET_PERMISSION_REQUEST));
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
    }
}
