package com.novus.navigo.services;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by sahajbedi on 11-Jan-16.
 */
public class LocationService {
    private final LocationManager mLocationManager;
    private static final String TAG = "LocationService";

    public LocationService(LocationManager locationManager) {
        mLocationManager = locationManager;
    }

    public Observable<Location> getLocation() {
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(final Subscriber<? super Location> subscriber) {

                final LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(final Location location) {
                        subscriber.onNext(location);
                        subscriber.onCompleted();

                        Looper.myLooper().quit();
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        Log.d(TAG, "Status Changed by provider : " + provider + ", with status " + status);
                    }

                    public void onProviderEnabled(String provider) {
                        Log.d(TAG, "Provider Enabled");
                    }

                    public void onProviderDisabled(String provider) {
                        Log.d(TAG, "Provider Disabled");
                    }
                };

                final Criteria locationCriteria = new Criteria();
                locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
                locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
                final String locationProvider = mLocationManager
                        .getBestProvider(locationCriteria, true);

                Looper.prepare();
                try {
                    mLocationManager.requestSingleUpdate(locationProvider,
                            locationListener, Looper.myLooper());
                } catch (SecurityException e) {
                    Log.d(TAG, "Security exception");
                }


                Looper.loop();
            }
        });
    }
}
