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

package com.miguelbcr.io.rx_gps_service.lib.entities;

import android.location.Location;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue public abstract class RouteStats {

  public abstract long time();

  public abstract long distance();

  public abstract float speedMax();

  public abstract float speedMin();

  public abstract float speedAverage();

  public abstract float speed();

  public abstract LatLongDetailed currentLocation();

  public abstract List<LatLong> latLongs();

  public abstract List<LatLongDetailed> latLongsDetailed();

  public static RouteStats create(long time, long distance, float speedMax, float speedMin,
      float speedAverage, float speed, LatLongDetailed currentLocation, List<LatLong> latLongs,
      List<LatLongDetailed> latLongsDetailed) {
    return new AutoValue_RouteStats(time, distance, speedMax, speedMin, speedAverage, speed,
        currentLocation, latLongs, latLongsDetailed);
  }

  public LatLong getLastLatLong() {
    return latLongs() != null && !latLongs().isEmpty() ? latLongs().get(latLongs().size() - 1)
        : LatLong.create(0, 0);
  }

  public LatLongDetailed getLastLatLongDetailed() {
    int size = latLongsDetailed().size();
    return latLongsDetailed() != null && !latLongsDetailed().isEmpty() ? latLongsDetailed().get(
        size - 1) : LatLongDetailed.create(new Location("Location-" + size));
  }

  @Override public String toString() {
    return " time="
        + time()
        + " distance="
        + distance()
        + " speed="
        + speed()
        + " latLong="
        + "("
        + currentLocation().location().getLatitude()
        + ", "
        + currentLocation().location().getLongitude()
        + ") checkpoint="
        + currentLocation().isCheckPoint()
        + " waypoints="
        + (latLongs() != null && !latLongs().isEmpty() ? latLongs().size()
        : latLongsDetailed() != null && !latLongsDetailed().isEmpty() ? latLongsDetailed().size()
            : 0);
  }
}
