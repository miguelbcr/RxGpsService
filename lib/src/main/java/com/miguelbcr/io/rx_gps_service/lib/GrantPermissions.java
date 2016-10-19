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
import com.miguelbcr.io.rx_gps_service.lib.entities.PermissionDeniedException;
import com.tbruyelle.rxpermissions.RxPermissions;
import rx.Observable;
import rx.functions.Func1;

final class GrantPermissions {
  private Activity activity;
  private String[] permissions;

  GrantPermissions(Activity activity) {
    this.activity = activity;
  }

  GrantPermissions with(String... permissions) {
    this.permissions = permissions;
    return this;
  }

  Observable<Void> builtObservable() {
    if (permissions.length == 0) {
      return Observable.just(null);
    }

    return RxPermissions.getInstance(activity)
        .request(permissions)
        .flatMap(new Func1<Boolean, Observable<Void>>() {
          @Override public Observable<Void> call(Boolean granted) {
            if (granted) {
              return Observable.just(null);
            }

            return Observable.error(
                new PermissionDeniedException("No permissions granted: " + permissionsToString()));
          }
        });
  }

  private String permissionsToString() {
    String permissionsStr = "";

    for (String permission : permissions) {
      permissionsStr += permission + "  ";
    }

    return permissionsStr;
  }
}
