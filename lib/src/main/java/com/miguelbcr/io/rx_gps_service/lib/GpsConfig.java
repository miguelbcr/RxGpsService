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

import android.app.Activity;
import com.google.android.gms.location.LocationRequest;

class GpsConfig {
  private Activity activity;
  private boolean debugMode, detailedWaypoints;
  private int stageDistance, minDistanceTraveled;
  private float speedMinModeAuto, discardSpeedsAbove;
  private int priority, interval, fastestInterval;

  GpsConfig(Activity activity) {
    this.activity = activity;
    this.debugMode = false;
    this.stageDistance = 0;
    this.minDistanceTraveled = 10;
    this.speedMinModeAuto = 5 / 3.6f;
    this.discardSpeedsAbove = 150 / 3.6f;
    this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
    this.interval = 10000;
    this.fastestInterval = 5000;
  }

  Activity getActivity() {
    return activity;
  }

  void setActivity(Activity activity) {
    this.activity = activity;
  }

  boolean isDebugMode() {
    return debugMode;
  }

  void setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
  }

  boolean isDetailedWaypoints() {
    return detailedWaypoints;
  }

  void setDetailedWaypoints(boolean detailedWaypoints) {
    this.detailedWaypoints = detailedWaypoints;
  }

  int getStageDistance() {
    return stageDistance;
  }

  void setStageDistance(int stageDistance) {
    this.stageDistance = stageDistance;
  }

  int getMinDistanceTraveled() {
    return minDistanceTraveled;
  }

  void setMinDistanceTraveled(int minDistanceTraveled) {
    this.minDistanceTraveled = minDistanceTraveled;
  }

  float getSpeedMinModeAuto() {
    return speedMinModeAuto;
  }

  void setSpeedMinModeAuto(float speedMinModeAuto) {
    this.speedMinModeAuto = speedMinModeAuto;
  }

  float getDiscardSpeedsAbove() {
    return discardSpeedsAbove;
  }

  void setDiscardSpeedsAbove(float discardSpeedsAbove) {
    this.discardSpeedsAbove = discardSpeedsAbove;
  }

  int getPriority() {
    return priority;
  }

  void setPriority(int priority) {
    this.priority = priority;
  }

  int getInterval() {
    return interval;
  }

  void setInterval(int interval) {
    this.interval = interval;
  }

  int getFastestInterval() {
    return fastestInterval;
  }

  void setFastestInterval(int fastestInterval) {
    this.fastestInterval = fastestInterval;
  }
}
