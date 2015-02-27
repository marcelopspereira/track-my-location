package com.techmagic.locationapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class TrackLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = TrackLocationService.class.getCanonicalName();
    private GoogleApiClient googleApiClient ;
    private Location startLocation;
    private Notification notification;

    public TrackLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createGoogleApiClient();
        connectGoogleApiClient();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        if (startLocation == null) {
            startLocation = location;
        }
        updateLocationData(location);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed");
    }

    private void createGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void connectGoogleApiClient() {
        if (googleApiClient != null) {
            if (!(googleApiClient.isConnected() || googleApiClient.isConnecting())) {
                googleApiClient.connect();
            } else {
                Log.d(TAG, "Client is connected");
                startLocationUpdates();
            }
        } else {
            Log.d(TAG, "Client is null");
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    private LocationRequest createLocationRequest() {
        LocationRequestData data = LocationRequestData.FREQUENCY_MEDIUM;
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(data.getInterval());
        locationRequest.setFastestInterval(data.getFastestInterval());
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void updateLocationData(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float distance = CoordinatesUtil.distFromCoordinates((float) startLocation.getLatitude(),
                (float) startLocation.getLongitude(),
                (float) latitude,
                (float) longitude);

        String distanceText = String.format("%.2f m.", distance);

        int notificationId = 9999;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Location Application")
                        .setContentText(distanceText);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notification = mBuilder.build();
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

}
