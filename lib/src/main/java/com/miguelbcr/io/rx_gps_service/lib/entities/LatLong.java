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

import com.google.auto.value.AutoValue;

@AutoValue public abstract class LatLong {
  public abstract double latitude();

  public abstract double longitude();

  public abstract float altitude();

  public abstract boolean isCheckPoint();

  public static LatLong create(double latitude, double longitude) {
    return create(latitude, longitude, 0, false);
  }

  public static LatLong create(double latitude, double longitude, float altitude,
      boolean isCheckPoint) {
    return new AutoValue_LatLong(latitude, longitude, altitude, isCheckPoint);
  }
}
