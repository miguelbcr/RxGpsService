package com.miguelbcr.rx_gpsservice.app;

import com.miguelbcr.io.rx_gps_service.lib.entities.RouteStats;

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
        return cacheProvider.readNullable();
    }

    void update(RouteStats routeStats) {
        Observable.just(routeStats)
                .compose(cacheProvider.replace())
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends RouteStats>>() {
                    @Override
                    public Observable<? extends RouteStats> call(Throwable throwable) {
                        return null;
                    }
                })
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
