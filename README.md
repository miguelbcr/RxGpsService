# RxGpsService

An Android service to retrieve GPS locations and [route stats]() using [RxJava](https://github.com/ReactiveX/RxJava)


## Features:

- Runtime permissions
- Highly customizable
- Always alive even if app is closed
- Provides route stats (time, speeds, distance and waypoints)
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
    compile "com.github.miguelbcr:RxGpsService:0.0.1"
    compile "io.reactivex:rxjava:1.1.8"
}
```


## Usage

### Starting the service

The basic usage is as follow, it will use the default configuration defined in [GpsConfig]()

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
                // Service is already started. Do your stuff
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
                // Service is already started. Do your stuff
            }

            @Override
            public void onNavigationModeChanged(boolean isAuto) {
                // Called when gps is switched from auto <-> manual
            }
        });
```

Additionally for [NotificationCompat.Builder]() you can set extra parameters in order to customize the notification created by RxGpsService:

```java
RxGpsService.builder(getActivity())
        .startService(new RxGpsService.Listener() {
            @Override
            public NotificationCompat.Builder notificationServiceStarted(Context context) {
                Bundle extras = new Bundle();
                extras.putBoolean(RxGpsServiceExtras.SHOW_TIME, true);
                extras.putString(RxGpsServiceExtras.TEXT_TIME, getString(R.string.time_elapsed));
                extras.putBoolean(RxGpsServiceExtras.SHOW_DISTANCE, true);
                extras.putString(RxGpsServiceExtras.TEXT_DISTANCE, getString(R.string.distance_traveled));
                extras.putString(RxGpsServiceExtras.BIG_CONTENT_TITLE, getString(R.string.route_details));

                return new NotificationCompat.Builder(context)
                               .setContentTitle(context.getString(R.string.app_name))
                               .setContentText(context.getString(R.string.route_started))
                               .setSmallIcon(R.drawable.ic_place)
                               .setExtras(extras);;
            }

            @Override
            public void onServiceAlreadyStarted() {
                // Service is already started. Do your stuff
            }

            @Override
            public void onNavigationModeChanged(boolean isAuto) {
                // Called when gps is switched from auto <-> manual
            }
        });
```

You can see all available options in [RxGpsServiceExtras]()

### Listening for location updates

In order to get the latest updated [RouteStats]() object, you will need to subscribe to the [RxGpsService#onRouteStatsUpdates()]() method, which is called every second, because of the chrono, but the [RouteStats]() could vary depending on the [GpsConfig]() used on the RxGpsService.Builder.

```java
public class RouteFragment extends Fragment {
    private CompositeSubscription subscriptions;
    ....

    @Override public void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscriptions != null) {
            subscriptions.unsubscribe();
            subscriptions = null;
        }
    }

    private void startListenForLocationUpdates() {
        if (RxGpsService.isServiceStarted()) {
            subscriptions = new CompositeSubscription();
            subscriptions.add(RxGpsService.instance().updatesRouteStats()
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
                            RxGpsService.stopService(getContext());;
                            unsubscribe();
                        }
                    }));
        }
    }

    ...
}

```


### Stoping and resuming the route trip

You can stop or resume the route trip by using [RxGpsService#stopChrono()]() or [RxGpsService#playChrono()]() or you can enable the navigation mode to auto by using [RxGpsService#setNavigationModeAuto()]() which will use `.withSpeedMinModeAuto()` to stop/resume the chrono if speed if lower/higher than specified respectively.
But even with the navigation in on mode auto you can also use [RxGpsService#stopChrono()]() or [RxGpsService#playChrono()]().

Notice that [RxGpsService#onRouteStatsUpdates()]() method will receive updates every seconds even when the [RxGpsService#stopChrono()]() is called, but the [RouteStats]() object will be the same just before when the chrono was stopped. So if you do not want to receive updates you only have to unsubscribe from the [RxGpsService#onRouteStatsUpdates()]() subscription.


## Example

You can see a complete example in [sample app]()

## Credits
* Runtime permissions: [RxPermissions](https://github.com/tbruyelle/RxPermissions)
* Reactive Location: [Android-ReactiveLocation](https://github.com/mcharmas/Android-ReactiveLocation)

## Authors

**Miguel García**

* <https://es.linkedin.com/in/miguelbcr>
* <https://github.com/miguelbcr>

## Another author's libraries:
* [RxPaparazzo](https://github.com/miguelbcr/RxPaparazzo): RxJava extension for Android to take images using camera and gallery.

## Another useful libraries used on the sample app:
* [ReactiveCache](https://github.com/VictorAlbertos/ReactiveCache): A reactive cache for Android and Java which honors the Observable chain.
* [Jolyglot](https://github.com/VictorAlbertos/Jolyglot): Agnostic Json abstraction to perform data binding operations for Android and Java.