package com.miguelbcr.rx_gpsservice.app;

import com.miguelbcr.rx_gps_service.lib.entities.RouteStats;

import io.reactivecache.Provider;
import io.reactivecache.ReactiveCache;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

class RouteStatsRepository {
    private final Provider<RouteStats> cacheProvider;

    RouteStatsRepository(ReactiveCache reactiveCache) {
        this.cacheProvider = reactiveCache.<RouteStats>provider()
                .withKey("RouteStats");
    }

    Observable<RouteStats> get() {
        RouteStats routeStats = null;
        return Observable.just(routeStats).compose(cacheProvider.readWithLoader());
    }

    void update(RouteStats routeStats) {
        Observable.just(routeStats)
                .compose(cacheProvider.replace())
                .map(new Func1<RouteStats, Void>() {
                    @Override
                    public Void call(RouteStats routeStats) {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
