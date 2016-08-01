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
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.miguelbcr.rx_gps_service.lib.entities.RouteStats;

import rx.Observable;


public class RxGpsService extends Service implements GpsServiceView {
    private static RxGpsService instance;
    private static Listener listener;
    private static GpsConfig gpsConfig;
    private RxGpsPresenter rxGpsPresenter;
    private Observable<RouteStats> oRouteStats;
    private boolean isChronoPlaying;

    public interface Listener {
        Notification statusBarNotification(Context context);
        void onServiceAlreadyStarted();
        void onNavigationModeChanged(boolean isAuto);
    }

    public static Builder builder(Activity activity) {
        return new Builder(activity);
    }

    public static class Builder {
        private GpsConfig gpsConfig;


        public Builder(Activity activity) {
            this.gpsConfig = new GpsConfig(activity);
        }

        /**
         * Shows extra information on debug mode
         *
         * @param debugMode
         * @return
         */
        public Builder withDebugMode(boolean debugMode) {
            this.gpsConfig.setDebugMode(debugMode);
            return this;
        }

        /**
         * Stage distance reached in meters to be notified
         *
         * @param stageDistance
         * @return
         */
        public Builder withStageDistance(int stageDistance) {
            this.gpsConfig.setStageDistance(stageDistance);
            return this;
        }

        /**
         * Min distance traveled in meters to be considered meaningful
         *
         * @param minDistanceTraveled
         * @return
         */
        public Builder withMinDistanceTraveled(int minDistanceTraveled) {
            this.gpsConfig.setMinDistanceTraveled(minDistanceTraveled);
            return this;
        }

        /**
         * Speed treshold in meters per second to start or pause chrono in mode auto
         *
         * @param speedMinModeAuto
         * @return
         */
        public Builder withSpeedMinModeAuto(float speedMinModeAuto) {
            this.gpsConfig.setSpeedMinModeAuto(speedMinModeAuto);
            return this;
        }

        /**
         * Discard higher speeds in meters per seconds
         *
         * @param discardSpeedsAbove
         * @return
         */
        public Builder withDiscardSpeedsAbove(float discardSpeedsAbove) {
            this.gpsConfig.setDiscardSpeedsAbove(discardSpeedsAbove);
            return this;
        }

        /**
         * Sets the {@link com.google.android.gms.location.LocationRequest} priority
         *
         * @param priority A {@link com.google.android.gms.location.LocationRequest} value
         * @return
         */
        public Builder withPriority(int priority) {
            this.gpsConfig.setPriority(priority);
            return this;
        }

        /**
         * Location update interval in milliseconds
         *
         * @param interval
         * @return
         */
        public Builder withInterval(int interval) {
            this.gpsConfig.setInterval(interval);
            return this;
        }

        /**
         * The fastest location update interval in milliseconds
         *
         * @param fastestInterval
         * @return
         */
        public Builder withFastestInterval(int fastestInterval) {
            this.gpsConfig.setFastestInterval(fastestInterval);
            return this;
        }

        public Builder withDetailedWaypoints(boolean detailed) {
            this.gpsConfig.setDetailedWaypoints(detailed);
            return this;
        }

        /**
         * {@link #instance()} object could not be available immediately just after call this method.<br/>
         * You should do your stuff on {@link Listener#onServiceAlreadyStarted()} method
         *
         * @param context
         */
        public void startService(Context context, Listener listener) {
            if (RxGpsService.instance == null) {
                RxGpsService.gpsConfig = this.gpsConfig;
                RxGpsService.listener = listener;
                context.startService(new Intent(context, RxGpsService.class));
            }
        }
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, RxGpsService.class));
    }

    public static RxGpsService instance() {
        return instance;
    }

    public static boolean isServiceStarted() {
        return instance != null;
    }

    @Override
    public void onCreate() {
        rxGpsPresenter = new RxGpsPresenter(gpsConfig);
        instance = this;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        rxGpsPresenter.attachView(this);

        startForeground(1, listener.statusBarNotification(getApplicationContext()));
        listener.onServiceAlreadyStarted();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxGpsService.gpsConfig.setActivity(null);
        RxGpsService.gpsConfig = null;
        RxGpsService.listener =  null;
        RxGpsService.instance = null;

        rxGpsPresenter.disposeSubscriptions();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void updatesRouteStats(Observable<RouteStats> oRouteStats) {
        this.oRouteStats = oRouteStats;

        if (rxGpsPresenter.isNavigationModeAuto()) {
            if (isChronoPlaying != isChronoPlaying()) {
                isChronoPlaying = isChronoPlaying();
                listener.onNavigationModeChanged(true);
            }
        }
    }

    public Observable<RouteStats> updatesRouteStats() {
        return oRouteStats;
    }

    public void playChrono() {
        rxGpsPresenter.playChrono();
    }

    public void stopChrono() {
        rxGpsPresenter.stopChrono();
    }

    public boolean isChronoPlaying() {
        return rxGpsPresenter.isChronoPlaying();
    }

    public void setNavigationModeAuto(boolean auto) {
        isChronoPlaying = isChronoPlaying();
        rxGpsPresenter.setNavigationModeAuto(auto);
        listener.onNavigationModeChanged(auto);
    }

    public boolean isNavigationModeAuto() {
        return rxGpsPresenter.isNavigationModeAuto();
    }
}
