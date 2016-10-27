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
import android.util.Log;

import com.miguelbcr.io.rx_gps_service.lib.entities.LatLong;
import com.miguelbcr.io.rx_gps_service.lib.entities.LatLongDetailed;
import com.miguelbcr.io.rx_gps_service.lib.entities.RouteStats;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

class RxGpsPresenter {
  private static final String TAG = "RxGpsService";
  private final MeaningfulUpdatesLocation meaningfulUpdatesLocation;
  private final RecordTime recordTime;
  private final GetTripDistance getTripDistance;
  private final GetTripSpeed getTripSpeed;
  private final GetTripSpeedAverage getTripSpeedAverage;
  private final GetTripSpeedMax getTripSpeedMax;
  private final GetTripSpeedMin getTripSpeedMin;
  private final Utilities utilities;
  private final CompositeSubscription subscriptions;
  private ConnectableObservable<RouteStats> connectableObservable;
  private List<LatLong> latLongs;
  private List<LatLongDetailed> latLongsDetailed;
  private long timeElapsed, lastTimeElapsed, timeElapsedBeingOnPause, timeLastLocation,
      distanceAccumulated, nextStageDistanceGoal;
  private boolean isMeaningfulWaypoint, stageDistanceReached, isNavigationModeAuto;
  private Location lastMeaningfulLocation, lastLocation;
  private float speed, speedAverage, speedMax, speedMin;
  private boolean isPlaying;

  private enum PERMISSIONS_STATE {DENIED, WAITING}

  private PERMISSIONS_STATE permissionState;
  private Throwable throwable;
  private GpsConfig gpsConfig;

  RxGpsPresenter(GpsConfig gpsConfig) {
    this.gpsConfig = gpsConfig;
    this.meaningfulUpdatesLocation = new MeaningfulUpdatesLocation();
    this.recordTime = new RecordTime();
    this.getTripDistance = new GetTripDistance();
    this.getTripSpeed = new GetTripSpeed();
    this.getTripSpeedAverage = new GetTripSpeedAverage();
    this.getTripSpeedMax = new GetTripSpeedMax();
    this.getTripSpeedMin = new GetTripSpeedMin();
    this.utilities = new Utilities();
    this.subscriptions = new CompositeSubscription();
    this.latLongs = new ArrayList<>();
    this.latLongsDetailed = new ArrayList<>();
    this.distanceAccumulated = 0;
    this.nextStageDistanceGoal = gpsConfig.getStageDistance();
    this.isMeaningfulWaypoint = false;
    this.stageDistanceReached = false;
    this.lastMeaningfulLocation = new Location("lastMeaningfulLocation");
    this.lastLocation = new Location("lastLocation");
    this.permissionState = PERMISSIONS_STATE.WAITING;
  }

  private boolean isEmpty(Location location) {
    return location.getLatitude() == 0 && location.getLongitude() == 0;
  }

  private LatLong getLatLong(Location location, boolean isCheckPoint) {
    return LatLong.create(location.getLatitude(), location.getLongitude(),
        (float) location.getAltitude(), isCheckPoint);
  }

  private LatLongDetailed getLatLongDetailed(Location location, boolean isCheckPoint) {
    return LatLongDetailed.create(location, isCheckPoint);
  }

  void attachView(RxGpsServiceView view) {
    // receive locations and filter them every location update interval
    subscriptions.add(new RxLocation(gpsConfig).getCurrentLocationForService()
        .flatMap(new Func1<Location, Observable<Float>>() {
          @Override public Observable<Float> call(Location location) {
            meaningfulUpdatesLocation.setCurrentLocation(location);
            if (isEmpty(lastLocation)) lastLocation = location;
            return utilities.getDistanceFromTo(getLatLong(lastLocation, false),
                getLatLong(location, false));
          }
        })
        .concatMap(new Func1<Float, Observable<Float>>() {
          @Override public Observable<Float> call(Float distance) {
            lastLocation = meaningfulUpdatesLocation.getCurrentLocation();
            long currentTime = System.currentTimeMillis() / 1000;
            if (timeLastLocation == 0) timeLastLocation = currentTime;
            timeLastLocation = currentTime - timeLastLocation;
            getTripSpeed.setParams(Math.round(distance), timeLastLocation,
                gpsConfig.getFastestInterval() / 1000,
                gpsConfig.getDiscardSpeedsAbove());
            timeLastLocation = currentTime;
            return getTripSpeed.builtObservable();
          }
        })
        .concatMap(new Func1<Float, Observable<Boolean>>() {
          @Override public Observable<Boolean> call(Float speed) {
            RxGpsPresenter.this.speed = speed;

            // Force to draw your position the first time
            if (isEmpty(lastMeaningfulLocation)) {
              return Observable.just(true);
            }

            if (isNavigationModeAuto) {
              if (speed < gpsConfig.getSpeedMinModeAuto()) {
                return Observable.just(false);
              } else {
                isPlaying = true;
              }
            } else if (!isPlaying) return Observable.just(false);

            meaningfulUpdatesLocation.setPreviousLocation(lastMeaningfulLocation);
            return meaningfulUpdatesLocation.builtObservable(gpsConfig.getMinDistanceTraveled());
          }
        })
        .subscribe(new Action1<Boolean>() {
          @Override public void call(Boolean isMeaningful) {
            if (isMeaningful) {
              lastMeaningfulLocation = meaningfulUpdatesLocation.getCurrentLocation();
              addWaypoint(lastMeaningfulLocation, false);
              isMeaningfulWaypoint = true;
            }
          }
        }, new Action1<Throwable>() {
          @Override public void call(Throwable throwable) {
            permissionState = PERMISSIONS_STATE.DENIED;
            RxGpsPresenter.this.throwable = throwable;
          }
        }));

    connectableObservable = getMeaningfulObservableRouteStats(recordTime.builtObservable()).publish();
    connectableObservable.connect();

    view.updatesRouteStats(connectableObservable);
  }

  private Observable<RouteStats> getMeaningfulObservableRouteStats(Observable<Long> oRecordTime) {
    // Creates an observable with the info to be displayed on fragment which is subscribed to it.
    // Will be updated each time step
    return oRecordTime.concatMap(new Func1<Long, Observable<Long>>() {
      @Override public Observable<Long> call(Long timeElapsedChrono) {
        if (permissionState == PERMISSIONS_STATE.DENIED && throwable != null) {
          return Observable.error(throwable);
        }
        timeElapsed = timeElapsedChrono;
        if (!isPlaying) timeElapsedBeingOnPause++;
        timeElapsed -= timeElapsedBeingOnPause;
        return Observable.just(timeElapsed);
      }
    }).concatMap(new Func1<Long, Observable<Long>>() {
      @Override public Observable<Long> call(Long timeElapsed) {
        if (!isMeaningfulWaypoint || isWaypointsEmpty() || hasOnlyOneWaypoint()) {
          return Observable.just(meaningfulUpdatesLocation.getLastMeaningfulDistance());
        }

        getTripDistance.setParams(distanceAccumulated, getLatLongBeforeLast(), getLastLatLong());
        return getTripDistance.builtObservable();
      }
    }).concatMap(new Func1<Long, Observable<Float>>() {
      @Override public Observable<Float> call(Long distance) {
        if (!isMeaningfulWaypoint) return Observable.just(speed);
        distanceAccumulated = getTripDistance.getDistanceAccumulated();
        lastTimeElapsed = timeElapsed - lastTimeElapsed;
        getTripSpeed.setParams(distance, lastTimeElapsed,
            gpsConfig.getFastestInterval() / 1000,
            gpsConfig.getDiscardSpeedsAbove());
        lastTimeElapsed = timeElapsed;
        return getTripSpeed.builtObservable();
      }
    }).concatMap(new Func1<Float, Observable<Float>>() {
      @Override public Observable<Float> call(Float speed) {
        if (!isMeaningfulWaypoint) return Observable.just(speedAverage);
        RxGpsPresenter.this.speed = speed;

        getTripSpeedAverage.setParams(distanceAccumulated, timeElapsed);
        return getTripSpeedAverage.builtObservable();
      }
    }).concatMap(new Func1<Float, Observable<Float>>() {
      @Override public Observable<Float> call(Float speedAverage) {
        if (!isMeaningfulWaypoint) return Observable.just(speedMax);
        RxGpsPresenter.this.speedAverage = speedAverage;

        getTripSpeedMax.setLastSpeed(getTripSpeed.getSpeed());
        return getTripSpeedMax.builtObservable();
      }
    }).concatMap(new Func1<Float, Observable<Float>>() {
      @Override public Observable<Float> call(Float speedMax) {
        if (!isMeaningfulWaypoint) return Observable.just(speedMin);
        RxGpsPresenter.this.speedMax = speedMax;

        getTripSpeedMin.setSpeedMinTreshold(gpsConfig.getSpeedMinModeAuto());
        getTripSpeedMin.setLastSpeed(getTripSpeed.getSpeed());
        return getTripSpeedMin.builtObservable();
      }
    }).map(new Func1<Float, RouteStats>() {
      @Override public RouteStats call(Float speedMin) {
        speedMin = (speedMin == GetTripSpeedMin.INITIAL_VALUE) ? 0f : speedMin;
        RxGpsPresenter.this.speedMin = speedMin;
        boolean isStageDistanceGoalReached = false;

        if (isStageDistanceGoalReached() && !isWaypointsEmpty()) {
          isStageDistanceGoalReached = true;
          resetFlagStageDistanceReached();
          replaceLastWaypoint(lastMeaningfulLocation, true);
        }

        printLog(lastMeaningfulLocation);
        isMeaningfulWaypoint = false;

        if (isNavigationModeAuto) {
          isPlaying = speed >= gpsConfig.getSpeedMinModeAuto();
        }

        return RouteStats.create(timeElapsed, distanceAccumulated, speedMax, speedMin, speedAverage,
            speed, LatLongDetailed.create(lastMeaningfulLocation, isStageDistanceGoalReached),
            latLongs, latLongsDetailed);
      }
    });
  }

  private void addWaypoint(Location location, boolean isCheckPoint) {
    if (gpsConfig.isDetailedWaypoints()) {
      latLongsDetailed.add(getLatLongDetailed(location, isCheckPoint));
    } else {
      latLongs.add(getLatLong(location, isCheckPoint));
    }
  }

  private void replaceLastWaypoint(Location location, boolean isCheckPoint) {
    if (gpsConfig.isDetailedWaypoints()) {
      latLongsDetailed.remove(latLongsDetailed.size() - 1);
      latLongsDetailed.add(getLatLongDetailed(location, isCheckPoint));
    } else {
      latLongs.remove(latLongs.size() - 1);
      latLongs.add(getLatLong(location, isCheckPoint));
    }
  }

  private boolean isWaypointsEmpty() {
    if (gpsConfig.isDetailedWaypoints()) {
      return latLongsDetailed.isEmpty();
    } else {
      return latLongs.isEmpty();
    }
  }

  private boolean hasOnlyOneWaypoint() {
    if (gpsConfig.isDetailedWaypoints()) {
      return latLongsDetailed.size() < 2;
    } else {
      return latLongs.size() < 2;
    }
  }

  private LatLong getLatLongBeforeLast() {
    if (gpsConfig.isDetailedWaypoints()) {
      LatLongDetailed latLongDetailed = latLongsDetailed.get(latLongsDetailed.size() - 2);
      return LatLong.create(latLongDetailed.location().getLatitude(),
          latLongDetailed.location().getLongitude());
    } else {
      return latLongs.get(latLongs.size() - 2);
    }
  }

  private LatLong getLastLatLong() {
    if (gpsConfig.isDetailedWaypoints()) {
      LatLongDetailed latLongDetailed = latLongsDetailed.get(latLongsDetailed.size() - 1);
      return LatLong.create(latLongDetailed.location().getLatitude(),
          latLongDetailed.location().getLongitude());
    } else {
      return latLongs.get(latLongs.size() - 1);
    }
  }

  void playChrono() {
    isPlaying = true;
  }

  void stopChrono() {
    isPlaying = false;
  }

  boolean isChronoPlaying() {
    return isPlaying;
  }

  void setNavigationModeAuto(boolean auto) {
    isNavigationModeAuto = auto;
  }

  boolean isNavigationModeAuto() {
    return isNavigationModeAuto;
  }

  void disposeSubscriptions() {
    connectableObservable.connect(new Action1<Subscription>() {
      @Override public void call(Subscription subscription) {
        subscription.unsubscribe();
      }
    });

    if (!subscriptions.isUnsubscribed()) {
      subscriptions.unsubscribe();
    }
  }

  private void resetFlagStageDistanceReached() {
    stageDistanceReached = false;
  }

  private boolean isStageDistanceGoalReached() {
    if (!stageDistanceReached && gpsConfig.getStageDistance() > 0) {
      if (distanceAccumulated > nextStageDistanceGoal) {
        if (gpsConfig.isDebugMode()) {
          Log.d(TAG, "stageDistanceReached (stage="
              + gpsConfig.getStageDistance()
              + ") at distance of "
              + distanceAccumulated);
        }

        stageDistanceReached = true;
        nextStageDistanceGoal += gpsConfig.getStageDistance();
      }
    }

    return stageDistanceReached;
  }

  private void printLog(Location location) {
    if (gpsConfig.isDebugMode()) {
      Log.d(TAG, "timeElapsed=" + timeElapsed +
          " lastTimeElapsed=" + getTripSpeed.getLastTimeElapsed() +
          " distance=" + distanceAccumulated +
          " lastDistance=" + meaningfulUpdatesLocation.getLastMeaningfulDistance() +
          " speed=" + speed +
          " speedAverage=" + speedAverage +
          " speedMax=" + speedMax +
          " speedMin=" + speedMin +
          " location=" + location.toString());
    }
  }
}
