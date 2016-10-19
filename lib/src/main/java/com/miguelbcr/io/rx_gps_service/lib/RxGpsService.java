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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.miguelbcr.io.rx_gps_service.lib.entities.RouteStats;
import rx.Observable;

public class RxGpsService extends Service implements RxGpsServiceView {
  private static RxGpsService instance;
  private static Listener listener;
  private static GpsConfig gpsConfig;
  private NotificationFactory notificationFactory;
  private RxGpsPresenter rxGpsPresenter;
  private Observable<RouteStats> oRouteStats;
  private boolean isChronoPlaying;

  public interface Listener {
    /**
     * A {@link android.support.v4.app.NotificationCompat.Builder} is required in order to
     * notify the service is running.<br/><br/>
     * You can use {@link com.miguelbcr.io.rx_gps_service.lib.entities.RxGpsServiceExtras} to show
     * additional info on the notification.
     *
     * @param context Use this context within this method body if any context is requiered.
     */
    NotificationCompat.Builder notificationServiceStarted(Context context);

    /**
     * This method is invoked when the service is already started.
     */
    void onServiceAlreadyStarted();

    /**
     * This method is invoked when {@link #setNavigationModeAuto(boolean)} is called or when
     * the mode {@code auto} is set and {@link #playChrono()} or {@link #stopChrono()} are called.
     */
    void onNavigationModeChanged(boolean isAuto);
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

  @Override public void onCreate() {
    rxGpsPresenter = new RxGpsPresenter(gpsConfig);
    instance = this;
    super.onCreate();
  }

  @Override public int onStartCommand(Intent intent, int flags, final int startId) {
    rxGpsPresenter.attachView(this);
    notificationFactory = new NotificationFactory(this, listener);

    startForeground(notificationFactory.getNotificationIdServiceStarted(),
        notificationFactory.getNotificationServiceStarted(0, 0));

    listener.onServiceAlreadyStarted();

    return Service.START_STICKY;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    RxGpsService.gpsConfig.setActivity(null);
    RxGpsService.gpsConfig = null;
    RxGpsService.listener = null;
    RxGpsService.instance = null;

    notificationFactory.onDestroy();
    rxGpsPresenter.disposeSubscriptions();
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void updatesRouteStats(Observable<RouteStats> oRouteStats) {
    this.oRouteStats = oRouteStats;

    if (rxGpsPresenter.isNavigationModeAuto()) {
      if (isChronoPlaying != isChronoPlaying()) {
        isChronoPlaying = isChronoPlaying();
        listener.onNavigationModeChanged(true);
      }
    }
  }

  public Observable<RouteStats> onRouteStatsUpdates() {
    notificationFactory.listenForUpdates(oRouteStats);

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

  public static Builder builder(Activity activity) {
    return new Builder(activity);
  }

  public static class Builder {
    private GpsConfig gpsConfig;

    Builder(Activity activity) {
      this.gpsConfig = new GpsConfig(activity);
    }

    /**
     * Shows extra information on debug mode.<br/>
     * Default false
     */
    public Builder withDebugMode(boolean debugMode) {
      this.gpsConfig.setDebugMode(debugMode);
      return this;
    }

    /**
     * Stage distance reached in meters to be notified.<br/>
     * Default 0
     */
    public Builder withStageDistance(int stageDistance) {
      this.gpsConfig.setStageDistance(stageDistance);
      return this;
    }

    /**
     * Min distance traveled in meters to be considered meaningful.<br/>
     * Default 10 meters
     */
    public Builder withMinDistanceTraveled(int minDistanceTraveled) {
      this.gpsConfig.setMinDistanceTraveled(minDistanceTraveled);
      return this;
    }

    /**
     * Speed treshold in meters per second to start or pause chrono in mode auto.<br/>
     * Default 5 km/h
     */
    public Builder withSpeedMinModeAuto(float speedMinModeAuto) {
      this.gpsConfig.setSpeedMinModeAuto(speedMinModeAuto);
      return this;
    }

    /**
     * Discard higher speeds in meters per seconds<br/>
     * Default 150 km/h
     */
    public Builder withDiscardSpeedsAbove(float discardSpeedsAbove) {
      this.gpsConfig.setDiscardSpeedsAbove(discardSpeedsAbove);
      return this;
    }

    /**
     * Sets the {@link com.google.android.gms.location.LocationRequest} priority.<br/>
     * Default {@link com.google.android.gms.location.LocationRequest#PRIORITY_HIGH_ACCURACY}
     *
     * @param priority A {@link com.google.android.gms.location.LocationRequest} value
     */
    public Builder withPriority(int priority) {
      this.gpsConfig.setPriority(priority);
      return this;
    }

    /**
     * Location update interval in milliseconds.<br/>
     * Default 10 seconds
     */
    public Builder withInterval(int interval) {
      this.gpsConfig.setInterval(interval);
      return this;
    }

    /**
     * The fastest location update interval in milliseconds.<br/>
     * Default 5 seconds
     */
    public Builder withFastestInterval(int fastestInterval) {
      this.gpsConfig.setFastestInterval(fastestInterval);
      return this;
    }

    /**
     * Uses simple or detailed waypoints for your route, but not both of them.<br/><br/>
     * See {@link RouteStats#latLongs()} and {@link RouteStats#latLongsDetailed()}.<br/>
     * Default false
     */
    public Builder withDetailedWaypoints(boolean detailed) {
      this.gpsConfig.setDetailedWaypoints(detailed);
      return this;
    }

    /**
     * {@link #instance()} object could not be available immediately just after call this
     * method.<br/>
     * You should do your stuff on {@link Listener#onServiceAlreadyStarted()} method
     */
    public void startService(Listener listener) {
      if (RxGpsService.instance == null) {
        RxGpsService.gpsConfig = this.gpsConfig;
        RxGpsService.listener = listener;
        Intent intent = new Intent(gpsConfig.getActivity(), RxGpsService.class);
        gpsConfig.getActivity().startService(intent);
      }
    }
  }
}
