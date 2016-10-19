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
import java.util.Locale;
import rx.Observable;

class Utilities {

  Utilities() {
  }

  Observable<Float> getDistanceFromTo(LatLong fromLatLong, LatLong toLatLong) {
    Location locationA = new Location("pointA");
    locationA.setLatitude(fromLatLong.latitude());
    locationA.setLongitude(fromLatLong.longitude());

    Location locationB = new Location("pointB");
    locationB.setLatitude(toLatLong.latitude());
    locationB.setLongitude(toLatLong.longitude());

    return Observable.just(locationA.distanceTo(locationB));
  }

  String getTimeFormatted(long seconds) {
    int hours = (int) (seconds / 3600);
    int minutes = (int) (seconds - hours * 3600) / 60;
    int secs = (int) (seconds - hours * 3600 - minutes * 60);

    return String.format(Locale.UK, "%02d:%02d'%02d\"", hours, minutes, secs);
  }

  String getDistanceFormatted(long distance) {
    if (distance < 1000) {
      return String.valueOf(distance) + " m";
    } else {
      return String.format(Locale.UK, "%.2f", distance / 1000f).replace(".", ",") + " Km";
    }
  }
}
