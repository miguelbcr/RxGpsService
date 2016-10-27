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

import android.location.Location;
import com.miguelbcr.io.rx_gps_service.lib.entities.LatLong;
import rx.Observable;
import rx.functions.Func1;

class MeaningfulUpdatesLocation {
  private Location previousLocation;
  private Location currentLocation;
  private final Utilities utilities;
  private long lastMeaningfulDistance;

  MeaningfulUpdatesLocation() {
    utilities = new Utilities();
  }

  void setPreviousLocation(Location previousLocation) {
    this.previousLocation = previousLocation;
  }

  void setCurrentLocation(Location currentLocation) {
    this.currentLocation = currentLocation;
  }

  Location getCurrentLocation() {
    return currentLocation;
  }

  long getLastMeaningfulDistance() {
    return lastMeaningfulDistance;
  }

  Observable<Boolean> builtObservable(final int minDistanceTraveled) {
    if (previousLocation == null) previousLocation = new Location("previousLocation");

    LatLong previousLatLng =
        LatLong.create(previousLocation.getLatitude(), previousLocation.getLongitude());
    LatLong currentLatLng =
        LatLong.create(currentLocation.getLatitude(), currentLocation.getLongitude());

    return utilities.getDistanceFromTo(previousLatLng, currentLatLng)
        .map(new Func1<Float, Boolean>() {
          @Override public Boolean call(Float distance) {
            boolean isMeaningful = minDistanceTraveled == 0 || distance >= minDistanceTraveled;

            if (isMeaningful) {
              lastMeaningfulDistance = Math.round(distance);
            }

            return isMeaningful;
          }
        });
  }
}
