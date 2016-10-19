/*
 * Copyright 2016 miguelbcr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.miguelbcr.io.rx_gps_service.lib;

import android.Manifest;
import android.content.IntentSender;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class RxLocation {
  private static final String TAG = "RxLocation";
  public static final int REQUEST_CHECK_LOCATION_SETTINGS = 65456;
  private final GrantPermissions grantPermissions;
  private GpsConfig gpsConfig;

  RxLocation(GpsConfig gpsConfig) {
    this.gpsConfig = gpsConfig;
    this.grantPermissions = new GrantPermissions(gpsConfig.getActivity());
  }

  Observable<Location> getCurrentLocationForService() {
    final ReactiveLocationProvider locationProvider =
        new ReactiveLocationProvider(gpsConfig.getActivity());

    final LocationRequest locationRequest = LocationRequest.create()
        .setPriority(gpsConfig.getPriority())
        .setInterval(gpsConfig.getInterval())
        .setFastestInterval(gpsConfig.getFastestInterval());

    final LocationSettingsRequest locationSettingsRequest =
        new LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            .setAlwaysShow(
                true)  //Reference: http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
            .build();

    return Observable.zip(
        grantPermissions.with(permissions()).builtObservable().subscribeOn(Schedulers.io()),
        locationProvider.checkLocationSettings(locationSettingsRequest)
            .subscribeOn(Schedulers.io()),
        new Func2<Void, LocationSettingsResult, LocationSettingsResult>() {
          @Override public LocationSettingsResult call(Void aVoid,
              LocationSettingsResult locationSettingsResult) {
            return locationSettingsResult;
          }
        }).doOnNext(new Action1<LocationSettingsResult>() {
      @Override public void call(LocationSettingsResult locationSettingsResult) {
        Status status = locationSettingsResult.getStatus();
        if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
          try {
            status.startResolutionForResult(gpsConfig.getActivity(),
                REQUEST_CHECK_LOCATION_SETTINGS);
          } catch (IntentSender.SendIntentException exception) {
            Log.e(TAG, "Error opening settings activity.", exception);
          }
        }
      }
    }).flatMap(new Func1<LocationSettingsResult, Observable<Location>>() {
      @Override public Observable<Location> call(LocationSettingsResult locationSettingsResult) {
        return locationProvider.getUpdatedLocation(locationRequest);
      }
    });
  }

  private String[] permissions() {
    return new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };
  }
}
