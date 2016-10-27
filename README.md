[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxGpsService-green.svg?style=true)](https://android-arsenal.com/details/1/4137)


# RxGpsService

An Android service to retrieve GPS locations and [route stats](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/entities/RouteStats.java) using [RxJava](https://github.com/ReactiveX/RxJava)


## Features:

- Runtime permissions
- Highly customizable
- Always alive even if app is closed
- Provides [route stats](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/entities/RouteStats.java) (time, speeds, distance and waypoints)
- Navigation mode auto/manual 
- Possibility to stop and resume the chrono at anytime


## Setup
Add the JitPack repository in your build.gradle (top level module):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

And add next dependencies in the build.gradle of the module:
```gradle
dependencies {
    compile "com.github.miguelbcr:RxGpsService:0.0.8"
    compile "io.reactivex:rxjava:1.1.10"
}
```


## Usage

### Starting the service

The basic usage is as follow, it will use the default configuration defined in [GpsConfig](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/GpsConfig.java)

```java
RxGpsService.builder(getActivity())
        .startService(new RxGpsService.Listener() {
            @Override
            public NotificationCompat.Builder notificationServiceStarted(Context context) {
                return new NotificationCompat.Builder(context)
                               .setContentTitle(context.getString(R.string.app_name))
                               .setContentText(context.getString(R.string.route_started))
                               .setSmallIcon(R.drawable.ic_place);
            }

            @Override
            public void onServiceAlreadyStarted() {
                // Service is already started. 
                // Now you can listen for location updates
                startListenForLocationUpdates();
            }

            @Override
            public void onNavigationModeChanged(boolean isAuto) {
                // Called when gps is switched from auto <-> manual
            }
        });
```

But you can start the RxGpsService using your custom config:

```java
RxGpsService.builder(getActivity())
        .withDebugMode(true)
        .withSpeedMinModeAuto(5 / 3.6f)
        .withStageDistance(0)
        .withDiscardSpeedsAbove(150 / 3.6f)
        .withMinDistanceTraveled(10)
        .withPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .withInterval(10000)
        .withFastestInterval(5000)
        .withDetailedWaypoints(false)
        .startService(new RxGpsService.Listener() {
            @Override
            public NotificationCompat.Builder notificationServiceStarted(Context context) {
                return new NotificationCompat.Builder(context)
                               .setContentTitle(context.getString(R.string.app_name))
                               .setContentText(context.getString(R.string.route_started))
                               .setSmallIcon(R.drawable.ic_place);
            }

            @Override
            public void onServiceAlreadyStarted() {
                // Service is already started. 
                // Now you can listen for location updates
                startListenForLocationUpdates();
            }

            @Override
            public void onNavigationModeChanged(boolean isAuto) {
                // Called when gps is switched from auto <-> manual
            }
        });
```

Additionally for [NotificationCompat.Builder](https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html) you can set extra parameters in order to customize the notification created by RxGpsService:

```java
RxGpsService.builder(getActivity())
        .startService(new RxGpsService.Listener() {
            @Override
            public NotificationCompat.Builder notificationServiceStarted(Context context) {
                Bundle extras = new Bundle();
                extras.putBoolean(RxGpsServiceExtras.SHOW_TIME, true);
                extras.putString(RxGpsServiceExtras.TEXT_TIME, context.getString(R.string.time_elapsed));
                extras.putBoolean(RxGpsServiceExtras.SHOW_DISTANCE, true);
                extras.putString(RxGpsServiceExtras.TEXT_DISTANCE, context.getString(R.string.distance_traveled));
                extras.putString(RxGpsServiceExtras.BIG_CONTENT_TITLE, context.getString(R.string.route_details));

                return new NotificationCompat.Builder(context)
                               .setContentTitle(context.getString(R.string.app_name))
                               .setContentText(context.getString(R.string.route_started))
                               .setSmallIcon(R.drawable.ic_place)
                               .setExtras(extras);
            }

            @Override
            public void onServiceAlreadyStarted() {
                // Service is already started. 
                // Now you can listen for location updates
                startListenForLocationUpdates();
            }

            @Override
            public void onNavigationModeChanged(boolean isAuto) {
                // Called when gps is switched from auto <-> manual
            }
        });
```

You can see all available options in [RxGpsServiceExtras](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/entities/RxGpsServiceExtras.java)

### Listening for location updates

In order to get the latest updated [RouteStats](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/entities/RouteStats.java) object, you will need to subscribe to the [RxGpsService#onRouteStatsUpdates()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) method, which is called every second, because of the chrono, but the [RouteStats](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/entities/RouteStats.java) could vary depending on the [GpsConfig](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/GpsConfig.java) used on the RxGpsService.Builder.

```java
public class RouteFragment extends Fragment {
    private Subscription subscription;
    ....

    @Override public void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private void startListenForLocationUpdates() {
        if (RxGpsService.isServiceStarted()) {
            unsubscribe()
            subscription = RxGpsService.instance().onRouteStatsUpdates()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<RouteStats>() {
                        @Override
                        public void call(RouteStats routeStats) {
                            drawUserPath(routeStats);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            RxGpsService.stopService(getContext());
                            unsubscribe();
                        }
                    });
        }
    }

    ...
}

```


### Stopping and resuming the route trip

You can stop or resume the route trip by using [RxGpsService#stopChrono()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) or [RxGpsService#playChrono()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) or you can enable the navigation mode to auto by using [RxGpsService#setNavigationModeAuto()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) which will use `builder.withSpeedMinModeAuto()` to stop/resume the chrono if speed if lower/higher than specified respectively.
But even with the navigation in on mode auto you can also use [RxGpsService#stopChrono()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) or [RxGpsService#playChrono()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java).

Notice that [RxGpsService#onRouteStatsUpdates()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) method will receive updates every seconds even when the [RxGpsService#stopChrono()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) is called, but the [RouteStats](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/entities/RouteStats.java) object will be the same just before when the chrono was stopped. So if you do not want to receive updates you only have to unsubscribe from the [RxGpsService#onRouteStatsUpdates()](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/RxGpsService.java) subscription.


## Example

You can see a complete example in [sample app](https://github.com/miguelbcr/RxGpsService/tree/master/app/src/main/java/com/miguelbcr/rx_gpsservice/app)

## A benchmark case

This is a memory size reference for [RouteStats](https://github.com/miguelbcr/RxGpsService/blob/master/lib/src/main/java/com/miguelbcr/io/rx_gps_service/lib/entities/RouteStats.java) running for 10 hour and emitting 1 meaningful waypoint per second:

* Using `builder.withDetailedWaypoints(false)`: ~ 3Mb
* Using `builder.withDetailedWaypoints(true)`: ~ 12Mb

## Credits
* Runtime permissions: [RxPermissions](https://github.com/tbruyelle/RxPermissions)
* Reactive Location: [Android-ReactiveLocation](https://github.com/mcharmas/Android-ReactiveLocation)

## Authors

**Miguel García**

* <https://es.linkedin.com/in/miguelbcr>
* <https://github.com/miguelbcr>

## Another author's libraries:
* [RxPaparazzo](https://github.com/miguelbcr/RxPaparazzo): RxJava extension for Android to take images using camera and gallery.
* [OkAdapters](https://github.com/miguelbcr/OkAdapters): Wrappers for Android adapters to simply its api at a minimum

## Another useful libraries used on the sample app:
* [ReactiveCache](https://github.com/VictorAlbertos/ReactiveCache): A reactive cache for Android and Java which honors the Observable chain.
* [Jolyglot](https://github.com/VictorAlbertos/Jolyglot): Agnostic Json abstraction to perform data binding operations for Android and Java.
* [ACRA](https://github.com/ACRA/acra): Application Crash Reports for Android
