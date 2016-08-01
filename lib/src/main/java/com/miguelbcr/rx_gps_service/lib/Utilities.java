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

package com.miguelbcr.rx_gps_service.lib;

import android.location.Location;

import com.miguelbcr.rx_gps_service.lib.entities.LatLong;

import rx.Observable;

class Utilities {

    Utilities() {
    }

    Observable<Float> getDistanceFromTo(LatLong fromLatLong, LatLong toLatLong) {
        Location locationA = new Location("pointA");
        locationA.setLatitude(fromLatLong.getLatitude());
        locationA.setLongitude(fromLatLong.getLongitude());

        Location locationB = new Location("pointB");
        locationB.setLatitude(toLatLong.getLatitude());
        locationB.setLongitude(toLatLong.getLongitude());

        return Observable.just(locationA.distanceTo(locationB));
    }
}
