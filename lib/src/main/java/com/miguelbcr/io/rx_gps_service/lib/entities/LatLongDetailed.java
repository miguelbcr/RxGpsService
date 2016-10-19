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

@AutoValue public abstract class LatLongDetailed {
  public abstract Location location();

  public abstract boolean isCheckPoint();

  public static LatLongDetailed create(Location location) {
    return create(location, false);
  }

  public static LatLongDetailed create(Location location, boolean isCheckPoint) {
    return new AutoValue_LatLongDetailed(location, isCheckPoint);
  }
}
