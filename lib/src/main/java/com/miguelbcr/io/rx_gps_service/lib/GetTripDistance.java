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

import com.miguelbcr.io.rx_gps_service.lib.entities.LatLong;
import rx.Observable;
import rx.functions.Func1;

class GetTripDistance {
  private long distanceAccumulated;
  private LatLong previousLatLong;
  private LatLong currentLatLong;
  private final Utilities utilities;

  GetTripDistance() {
    utilities = new Utilities();
  }

  void setParams(long distanceAccumulated, LatLong previousLatLong, LatLong currentLatLong) {
    this.distanceAccumulated = distanceAccumulated;
    this.previousLatLong = previousLatLong;
    this.currentLatLong = currentLatLong;
  }

  long getDistanceAccumulated() {
    return distanceAccumulated;
  }

  Observable<Long> builtObservable() {
    return utilities.getDistanceFromTo(previousLatLong, currentLatLong)
        .concatMap(new Func1<Float, Observable<? extends Long>>() {
          @Override public Observable<? extends Long> call(Float distance) {
            long lastDistance = Math.round(distance);
            distanceAccumulated += lastDistance;
            return Observable.just(lastDistance);
          }
        });
  }
}
