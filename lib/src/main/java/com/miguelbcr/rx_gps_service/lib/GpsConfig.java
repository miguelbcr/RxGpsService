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
        this.stageDistance = 1000;
        this.minDistanceTraveled = 10;
        this.speedMinModeAuto = 5 / 3.6f;
        this.discardSpeedsAbove = 150 / 3.6f;
        this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        this.interval = 10000;
        this.fastestInterval = 5000;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDetailedWaypoints() {
        return detailedWaypoints;
    }

    public void setDetailedWaypoints(boolean detailedWaypoints) {
        this.detailedWaypoints = detailedWaypoints;
    }

    public int getStageDistance() {
        return stageDistance;
    }

    public void setStageDistance(int stageDistance) {
        this.stageDistance = stageDistance;
    }

    public int getMinDistanceTraveled() {
        return minDistanceTraveled;
    }

    public void setMinDistanceTraveled(int minDistanceTraveled) {
        this.minDistanceTraveled = minDistanceTraveled;
    }

    public float getSpeedMinModeAuto() {
        return speedMinModeAuto;
    }

    public void setSpeedMinModeAuto(float speedMinModeAuto) {
        this.speedMinModeAuto = speedMinModeAuto;
    }

    public float getDiscardSpeedsAbove() {
        return discardSpeedsAbove;
    }

    public void setDiscardSpeedsAbove(float discardSpeedsAbove) {
        this.discardSpeedsAbove = discardSpeedsAbove;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getFastestInterval() {
        return fastestInterval;
    }

    public void setFastestInterval(int fastestInterval) {
        this.fastestInterval = fastestInterval;
    }
}
