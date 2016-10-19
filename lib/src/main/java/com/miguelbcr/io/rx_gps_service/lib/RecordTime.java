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

import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;

class RecordTime {
  private ConnectableObservable<Long> oChronometer;
  private Subscriber<Long> subscriber;

  RecordTime() {
    oChronometer = Observable.<Long>create(new Observable.OnSubscribe<Long>() {
      @Override public void call(Subscriber<? super Long> subscriber) {
        RecordTime.this.subscriber = (Subscriber<Long>) subscriber;
      }
    }).publish();
    oChronometer.connect();

    Observable.interval(1, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
      @Override public void call(Long elapsedTime) {
        subscriber.onNext(elapsedTime);
      }
    });
  }

  Observable<Long> builtObservable() {
    return oChronometer;
  }
}
