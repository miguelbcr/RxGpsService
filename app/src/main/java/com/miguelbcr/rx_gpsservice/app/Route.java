package com.miguelbcr.rx_gpsservice.app;

import com.miguelbcr.rx_gps_service.lib.entities.LatLong;

import java.util.List;

public class Route  extends Place {
    private List<LatLong> path;
    private LatLong latLongEnd;

    public Route() {
    }

    public List<LatLong> getPath() {
        return path;
    }

    public void setPath(List<LatLong> path) {
        this.path = path;
    }

    public LatLong getLatLongEnd() {
        return latLongEnd;
    }

    public void setLatLongEnd(LatLong latLongEnd) {
        this.latLongEnd = latLongEnd;
    }
}
