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

import android.os.Bundle;

import com.google.auto.value.AutoValue;

/**
 * Extras to use on {@link android.support.v4.app.NotificationCompat.Builder#setExtras(Bundle)}.
 * <br/><br/>
 *
 * NOTICE: A notification's big view appears only when the notification is expanded, which happens
 * when the notification is at the top of the notification drawer, or when the user expands the
 * notification with a gesture.
 */
@AutoValue public abstract class RxGpsServiceExtras {
  /**
   * Custom id for notification. Default 1.
   */
  public static final String NOTIFICATION_ID_SERVICE_STARTED = "notificationIdServiceStarted";
  /**
   * Custom group name for notification expanded view. Default "ServiceStartedGroup".
   */
  public static final String NOTIFICATION_GROUP_SERVICE_STARTED = "notificationGroupServiceStarted";
  /**
   * Shows the content title for the expanded view. Default empty.
   */
  public static final String BIG_CONTENT_TITLE = "extrasBigContentTitle";
  /**
   * Shows chrono time in format HH:mm:ss on a expanded layout. Default false.<br/><br/>
   * See {@link #TEXT_TIME}
   */
  public static final String SHOW_TIME = "extrasShowTime";
  /**
   * if {@link #SHOW_TIME} is set to true, this text will be used as text on the notification
   * replacing %1$s by the chrono time. Default empty.<br/><br/>
   * See {@link #SHOW_TIME}
   */
  public static final String TEXT_TIME = "extrasTextTime";
  /**
   * Shows distance traveled in format m or km on a expanded view. Default false.<br/><br/>
   * See {@link #TEXT_DISTANCE}
   */
  public static final String SHOW_DISTANCE = "extrasShowDistance";
  /**
   * if {@link #SHOW_DISTANCE} is set to true, this text will be used as text on the notification
   * replacing %1$s by the distance traveled. Default empty.<br/><br/>
   * See {@link #SHOW_DISTANCE}
   */
  public static final String TEXT_DISTANCE = "extrasTextDistance";

  public abstract int notificationIdServiceStarted();

  public abstract String notificationGroupServiceStarted();

  public abstract String bigContentTitle();

  public abstract boolean showTime();

  public abstract String timeText();

  public abstract boolean showDistance();

  public abstract String distanceText();

  private static RxGpsServiceExtras create(int notificationIdServiceStarted,
      String notificationGroupServiceStarted, String bigContentTitle, boolean showTime,
      String timeText, boolean showDistance, String distanceText) {
    return new AutoValue_RxGpsServiceExtras(notificationIdServiceStarted,
        notificationGroupServiceStarted, bigContentTitle, showTime, timeText, showDistance,
        distanceText);
  }

  public static RxGpsServiceExtras createFromBundle(Bundle extras) {
    int notificationIdServiceStarted = extras.getInt(NOTIFICATION_ID_SERVICE_STARTED, 1);
    String notificationGroupServiceStarted = extras.getString(NOTIFICATION_GROUP_SERVICE_STARTED);
    if (notificationGroupServiceStarted == null) {
      notificationGroupServiceStarted = "ServiceStartedGroup";
    }

    String bigContentTitle = extras.getString(BIG_CONTENT_TITLE);
    if (bigContentTitle == null) {
      bigContentTitle = "";
    }

    boolean showTime = extras.getBoolean(SHOW_TIME, false);
    String timeText = extras.getString(TEXT_TIME);
    if (timeText == null) {
      timeText = "";
    }

    boolean showDistance = extras.getBoolean(SHOW_DISTANCE, false);
    String distanceText = extras.getString(TEXT_DISTANCE);
    if (distanceText == null) {
      distanceText = "";
    }

    return RxGpsServiceExtras.create(notificationIdServiceStarted, notificationGroupServiceStarted,
        bigContentTitle, showTime, timeText, showDistance, distanceText);
  }
}
