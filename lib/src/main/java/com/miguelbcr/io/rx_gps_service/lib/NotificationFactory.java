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

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import com.miguelbcr.io.rx_gps_service.lib.entities.RouteStats;
import com.miguelbcr.io.rx_gps_service.lib.entities.RxGpsServiceExtras;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;

class NotificationFactory {
  private Context context;
  private RxGpsService.Listener listener;
  private Utilities utilities;
  private ConnectableObservable<RouteStats> connectableObservable;
  private RxGpsServiceExtras rxGpsServiceExtras;
  private NotificationCompat.Builder builderServiceStarted;
  private NotificationManagerCompat notificationManager;

  NotificationFactory(Context context, RxGpsService.Listener listener) {
    this.context = context;
    this.listener = listener;
    utilities = new Utilities();
    notificationManager = NotificationManagerCompat.from(context);
    loadNotificationBuilders();
    loadNotificationExtras();
  }

  private void loadNotificationBuilders() {
    builderServiceStarted = listener.notificationServiceStarted(context);
  }

  private void loadNotificationExtras() {
    Bundle extras = builderServiceStarted.getExtras();
    rxGpsServiceExtras = RxGpsServiceExtras.createFromBundle(extras);
  }

  Notification getNotificationServiceStarted(long seconds, long distance) {
    boolean showGroup = false;
    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
    String timeFormatted = utilities.getTimeFormatted(seconds);
    String distanceFormatted = utilities.getDistanceFormatted(distance);

    if (!TextUtils.isEmpty(rxGpsServiceExtras.bigContentTitle())) {
      inboxStyle.setBigContentTitle(rxGpsServiceExtras.bigContentTitle());
    }

    if (rxGpsServiceExtras.showTime()) {
      showGroup = true;
      timeFormatted = TextUtils.isEmpty(rxGpsServiceExtras.timeText()) ? timeFormatted
          : rxGpsServiceExtras.timeText().replace("%1$s", timeFormatted);
      inboxStyle.addLine(timeFormatted);
    }

    if (rxGpsServiceExtras.showDistance()) {
      showGroup = true;
      distanceFormatted = TextUtils.isEmpty(rxGpsServiceExtras.distanceText()) ? distanceFormatted
          : rxGpsServiceExtras.distanceText().replace("%1$s", distanceFormatted);
      inboxStyle.addLine(distanceFormatted);
    }

    if (showGroup) {
      builderServiceStarted.setPriority(NotificationCompat.PRIORITY_MAX);
      builderServiceStarted.setOngoing(true);
      builderServiceStarted.setAutoCancel(false);
      builderServiceStarted.setStyle(inboxStyle);
      builderServiceStarted.setGroupSummary(true);
      builderServiceStarted.setGroup(rxGpsServiceExtras.notificationGroupServiceStarted());
    }

    return builderServiceStarted.build();
  }

  void onDestroy() {
    if (connectableObservable != null) {
      connectableObservable.connect(new Action1<Subscription>() {
        @Override public void call(Subscription subscription) {
          subscription.unsubscribe();
        }
      });
    }
  }

  int getNotificationIdServiceStarted() {
    return rxGpsServiceExtras.notificationIdServiceStarted();
  }

  void listenForUpdates(Observable<RouteStats> oRouteStats) {
    if (rxGpsServiceExtras.showTime()) {
      if (connectableObservable == null) {
        connectableObservable = oRouteStats.publish();
        connectableObservable.connect();
      }

      connectableObservable.subscribe(new Action1<RouteStats>() {
        @Override public void call(RouteStats routeStats) {
          notificationManager.notify(rxGpsServiceExtras.notificationIdServiceStarted(),
              getNotificationServiceStarted(routeStats.time(), routeStats.distance()));
        }
      }, new Action1<Throwable>() {
        @Override public void call(Throwable throwable) {
          // nothing
        }
      });
    }
  }
}
