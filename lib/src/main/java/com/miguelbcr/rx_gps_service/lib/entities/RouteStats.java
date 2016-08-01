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

package com.miguelbcr.rx_gps_service.lib.entities;


import android.location.Location;

import java.util.List;

public class RouteStats {
    private final long time, distance;
    private final float speedMax, speedMin, speedAverage, speed;
    private final LatLongDetailed currentLocation;
    private final List<LatLong> latLongs;
    private final List<LatLongDetailed> latLongsDetailed;


    public RouteStats(long time, long distance, float speedMax, float speedMin, float speedAverage, float speed, LatLongDetailed currentLocation, List<LatLong> latLongs, List<LatLongDetailed> latLongsDetailed) {
        this.time = time;
        this.distance = distance;
        this.speedMax = speedMax;
        this.speedMin = speedMin;
        this.speedAverage = speedAverage;
        this.speed = speed;
        this.currentLocation = currentLocation;
        this.latLongs = latLongs;
        this.latLongsDetailed = latLongsDetailed;
    }

    public long getTime() {
        return time;
    }

    public long getDistance() {
        return distance;
    }

    public float getSpeedMax() {
        return speedMax;
    }

    public float getSpeedMin() {
        return speedMin;
    }

    public float getSpeedAverage() {
        return speedAverage;
    }

    public float getSpeed() {
        return speed;
    }

    public LatLongDetailed getCurrentLocation() {
        return currentLocation;
    }

    public List<LatLong> getLatLongs() {
        return latLongs;
    }

    public LatLong getLastLatLong() {
        return latLongs != null && !latLongs.isEmpty() ? latLongs.get(latLongs.size() - 1) : new LatLong(0, 0);
    }

    public List<LatLongDetailed> getLatLongsDetailed() {
        return latLongsDetailed;
    }

    public LatLongDetailed getLastLatLongDetailed() {
        int size = latLongsDetailed.size();
        return latLongsDetailed != null && !latLongsDetailed.isEmpty() ? latLongsDetailed.get(size - 1) : new LatLongDetailed(new Location("Location-" + size));
    }

    @Override
    public String toString() {
        return " time=" + time +
                " distance=" + distance +
                " speed=" + speed +
                " latLong=" + "(" + currentLocation.getLocation().getLatitude() + ", " + currentLocation.getLocation().getLongitude() + ")";
    }
}
