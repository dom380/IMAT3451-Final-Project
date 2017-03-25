package dmu.mygdx.game;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
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
 * <p>
 * Implementation of LocationService using Google Play Services.
 */

public class LocationServiceAndroid implements LocationService, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GameFragment mLauncher;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsAvailable, mConnected = false, mEnabled;
    private Location mLastLocation;

    /**
     * Constructor.
     *
     * @param activity Fragment Activity containing our game.
     */
    LocationServiceAndroid(GameFragment activity) {
        this.mLauncher = activity;
        mEnabled = true;
        GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
        int playServicesAvailable = instance.isGooglePlayServicesAvailable(mLauncher.getContext());
        if (playServicesAvailable == ConnectionResult.SUCCESS) { //If we have access to Google Play Services
            mIsAvailable = true;
            this.mGoogleApiClient = new GoogleApiClient.Builder(activity.getContext()) //Create a self managing client
                    .enableAutoManage(mLauncher.getActivity(), mLauncher)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            connect();
        } else {
            Dialog errorDialog = instance.getErrorDialog(mLauncher.getActivity(), playServicesAvailable, 1);
            errorDialog.show();
            mIsAvailable = false;
        }
    }

    /**
     * Connects to the Google Play Service.
     */
    @Override
    public void connect() {
        mGoogleApiClient.connect();
        mConnected = true;
    }

    /**
     * Disconnects from the Google Play Service.
     */
    @Override
    public void disconnect() {
        mGoogleApiClient.disconnect();
        mConnected = false;
    }

    @Override
    public boolean isAvailable() {
        return mIsAvailable && mEnabled;
    }

    /**
     * Retrieves the devices mLast known latitude and longitude.
     *
     * @return A two value array holding the latitude and longitude or null if not location available.
     */
    @Override
    public double[] getLatLong() {
        if (!mEnabled) {
            return null;
        }
        if (mLastLocation != null)
            return new double[]{mLastLocation.getLatitude(), mLastLocation.getLongitude()};
        else {
            //noinspection MissingPermission
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return mLastLocation != null ? new double[]{mLastLocation.getLatitude(), mLastLocation.getLongitude()} : null;
        }

    }

    @Override
    public void disable() {
        mEnabled = false;
    }

    @Override
    public void enable() {
        mEnabled = true;
    }

    /**
     * Callback method executed Google Play Service is connected.
     * Builds a LocationRequest to continually update devices location every minute.
     *
     * @param bundle Parameter bundle.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(120000) //2 min
                .setFastestInterval(60000); //1 min
        if (ActivityCompat.checkSelfPermission(mLauncher.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(mLauncher.getContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(mLauncher.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT > 23 && ActivityCompat.shouldShowRequestPermissionRationale(mLauncher.getActivity(), mLauncher.getString(R.string.location_explanation))) {
                //Check to see if we should explain the request
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(mLauncher.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                //Request permissions
                ActivityCompat.requestPermissions(mLauncher.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, mLauncher.getResources().getInteger(R.integer.LOCATION_PERMISSION_REQUEST));
                ActivityCompat.requestPermissions(mLauncher.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, mLauncher.getResources().getInteger(R.integer.LOCATION_PERMISSION_REQUEST));
                ActivityCompat.requestPermissions(mLauncher.getActivity(), new String[]{Manifest.permission.INTERNET}, mLauncher.getResources().getInteger(R.integer.INTERNET_PERMISSION_REQUEST));
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }
}
