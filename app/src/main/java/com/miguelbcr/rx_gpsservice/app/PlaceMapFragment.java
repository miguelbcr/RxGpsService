package com.miguelbcr.rx_gpsservice.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.miguelbcr.rx_gps_service.lib.entities.LatLong;
import com.miguelbcr.rx_gps_service.lib.entities.RouteStats;
import com.miguelbcr.rx_gpsservice.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PlaceMapFragment extends SupportMapFragment {
    private static final float DEFAULT_ZOOM = 16f;
    private static final LatLng DEFAULT_LATLNG = new LatLng(39.62261494, 2.98965454);
    BitmapHelper bitmapHelper;
    private Place place;
    private List<? extends Place> places;
    private GoogleMap googleMap;
    private Map<Marker, Object> markersMap = new HashMap<>();
    private Listener listener;
    private BitmapDescriptor iconRoute, iconPoi, iconUser;
    private Polyline polylineRoute;
    private Polyline polylineUser;
    private Polyline polylineUserLastPath;
    private Marker markerUser;


    public interface Listener  {
        void setOnInfoWindowClickListener(Place place);
    }

    public static PlaceMapFragment getInstance() {
        PlaceMapFragment fragment = new PlaceMapFragment();
        return fragment;
    }

    public static PlaceMapFragment getInstance(Place place, Listener listener) {
        PlaceMapFragment fragment = new PlaceMapFragment();
        fragment.place = place;
        fragment.listener = listener;
        return fragment;
    }

    public static PlaceMapFragment getInstance(List<Place> places, Listener listener) {
        PlaceMapFragment fragment = new PlaceMapFragment();
        fragment.places = places;
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bitmapHelper = new BitmapHelper();
        initViews();
    }

    private void initViews() {
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                googleMap = gMap;
                googleMap.getUiSettings().setAllGesturesEnabled(true);
                googleMap.clear();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LATLNG, DEFAULT_ZOOM));
                setCustomInfoWindow();
                PlaceMapFragment.this.setMapListeners();
                if (place != null) PlaceMapFragment.this.showPlace(place, false, 0, listener);
                if (places != null && !places.isEmpty())
                    PlaceMapFragment.this.showPlaces(places, false, places.get(0).getId(), listener);
            }
        });
    }

    @Override
    public void onDestroy() {
        iconRoute = null;
        iconPoi = null;
        iconUser = null;

        polylineRoute = removePath(polylineRoute);
        polylineUser = removePath(polylineUser);
        polylineUserLastPath = removePath(polylineUserLastPath);

        if (markersMap != null) {
            markersMap.clear();
            markersMap = null;
        }

        if (googleMap != null) {
            // Avoid memory leak: https://code.google.com/p/gmaps-api-issues/issues/detail?id=8111
//            googleMap.setMyLocationEnabled(false);
            googleMap.clear();
            googleMap = null;
        }

        super.onDestroy();
    }

    private Polyline removePath(Polyline polyline) {
        if (polyline != null) {
            polyline.remove();
        }
        return null;
    }

    public void applyZoom(float zooom) {
        if (googleMap == null) {
            return;
        }
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(zooom));
    }

    public void showPlace(Place place, boolean clearMap, int idPlaceToGo, Listener listener) {
        if (googleMap == null) {
            return;
        }
        if (clearMap) {
            googleMap.clear();
        }
        this.place = place;
        this.listener = listener;

        if (place instanceof Route) {
            Route route = (Route) place;
            if (route.getId() == 0)  return; // Creating new route. Route is empty

            LatLng latLng = new LatLng(route.getLatLong().latitude(), route.getLatLong().longitude());
            markersMap.put(addMark(latLng, route.getName(), getIconRoute(), route.getId() == idPlaceToGo), route);
            latLng = new LatLng(route.getLatLongEnd().latitude(), route.getLatLongEnd().longitude());
            markersMap.put(addMark(latLng, route.getName(), getIconRoute(), false), route);
            polylineRoute = removePath(polylineRoute);
            polylineRoute = drawPath(route.getPath(), ContextCompat.getColor(getContext(), R.color.blue), 1f, polylineRoute);
        } else {
            if (TextUtils.isEmpty(place.getUrlImage())) {
                markersMap.put(addMarkerPoi(place, getIconPoi(), idPlaceToGo), place);
            }
            else {
                Picasso.with(getContext()).load(place.getUrlImage()).into(getTargetForPois(place, idPlaceToGo));
            }
        }
    }

    public void showPlaces(List<? extends Place> places, boolean clearMap, int idPlaceToGo, Listener listener) {
        if (googleMap == null) {
            return;
        }

        if (clearMap) {
            googleMap.clear();
        }

        this.places = places;
        this.listener = listener;

        for (Place place : places) {
            showPlace(place, false, idPlaceToGo, listener);
            if (place.getId() == idPlaceToGo) {
                showMarkInfoWindow(idPlaceToGo);
            }
        }
    }

    public void disableNavigationControls() {
        if (googleMap == null) return;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    public void navigateTo(double latitude, double longitude) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + ", " + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void drawPathUser(List<LatLong> waypoints) {
        if (googleMap == null) return;
        if (polylineUser != null && polylineUser.getPoints().size() == waypoints.size()) return;

        polylineUser = removePath(polylineUser);
        polylineUserLastPath = removePath(polylineUserLastPath);
        polylineUser = drawPath(waypoints, ContextCompat.getColor(getContext(), R.color.blue), 2f, polylineUser);

        if (waypoints != null && !waypoints.isEmpty()) {
            showCheckpoints(waypoints);
            LatLong lastLatLong = waypoints.get(waypoints.size() - 1);
            LatLng latLng = new LatLng(lastLatLong.latitude(), lastLatLong.longitude());
            drawSegmentPathUser(latLng);
        }
    }

    public void updateUserLocation(RouteStats routeStats, boolean goToUserLocation) {
        LatLong currentLocation = routeStats.getLastLatLong();

        if (googleMap == null || isEmptyLatLong(currentLocation)) return;

        LatLng latLng = new LatLng(currentLocation.latitude(), currentLocation.longitude());
        drawSegmentPathUser(latLng);
        if (markerUser != null) markerUser.remove();
        markerUser = addMark(latLng, "", getIconUser(), false);
        markersMap.put(markerUser, routeStats);
        markerUser.showInfoWindow();

        if (goToUserLocation) googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void resetUserPosition(LatLong latLong) {
        if (googleMap == null || isEmptyLatLong(latLong)) return;

        LatLng latLng = new LatLng(latLong.latitude(), latLong.longitude());
        if (markerUser != null) markerUser.remove();
        markerUser = addMark(latLng, "", getIconUser(), false);
        markerUser.hideInfoWindow();
        markersMap.put(markerUser, RouteStats.create(0, 0, 0f, 0f, 0f, 0f, null, null, null));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private boolean isEmptyLatLong(LatLong latLong) {
        return latLong == null || (latLong.latitude() == 0 && latLong.longitude() == 0);
    }

    private void showCheckpoints(List<LatLong> latLongs) {
        List<Place> places = new ArrayList<>();

        for (LatLong latLong : latLongs) {
            if (latLong.isCheckPoint()) {
                Place checkPoint = new Place();
                checkPoint.setId(latLongs.size());
                checkPoint.setLatLong(latLong);
                places.add(checkPoint);
            }
        }

        showPlaces(places, false, 0, null);
    }

    private BitmapDescriptor getIconRoute() {
        if (iconRoute == null) {
            Bitmap bitmap = bitmapHelper.getBitmap(getContext(), R.drawable.ic_assistant_photo);
            bitmap = bitmapHelper.getTintedBitmap(bitmap, ContextCompat.getColor(getContext(), R.color.red));
            iconRoute = BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return iconRoute;
    }

    private BitmapDescriptor getIconUser() {
        if (iconUser == null) {
            Bitmap bitmap = bitmapHelper.getBitmap(getContext(), R.drawable.ic_accessibility);
            bitmap = bitmapHelper.getTintedBitmap(bitmap, ContextCompat.getColor(getContext(), R.color.green));
            iconUser = BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return iconUser;
    }

    private BitmapDescriptor getIconPoi() {
        if (iconPoi == null) {
            Bitmap bitmap = bitmapHelper.getBitmap(getContext(), R.drawable.ic_place);
            bitmap = bitmapHelper.getTintedBitmap(bitmap, ContextCompat.getColor(getContext(), R.color.orange));
            iconPoi = BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return iconPoi;
    }

    private Marker addMark(LatLng latLng, String title, BitmapDescriptor icon, boolean goToMark) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(title).icon(icon));
        if (goToMark) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        return marker;
    }

    private Marker addMarkerPoi(final Place place, final BitmapDescriptor bitmapDescriptor, final int idPlaceToGo) {
        LatLng latLng = new LatLng(place.getLatLong().latitude(), place.getLatLong().longitude());
        return addMark(latLng, place.getName(), bitmapDescriptor, place.getId() == idPlaceToGo);
    }

    private Target getTargetForPois(final Place place, final int idPlaceToGo) {
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                bitmap = bitmapHelper.getTintedBitmap(bitmap, ContextCompat.getColor(getContext(), R.color.orange));
                bitmap = bitmapHelper.getScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen._30dp));
                markersMap.put(addMarkerPoi(place, BitmapDescriptorFactory.fromBitmap(bitmap), idPlaceToGo), place);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                markersMap.put(addMarkerPoi(place, getIconPoi(), idPlaceToGo), place);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }

    private void showMarkInfoWindow(int objectId) {
        if (objectId > 0) {
            for (Map.Entry<Marker, Object> entry : markersMap.entrySet()) {
                Object object = entry.getValue();

                if (object instanceof Place) {
                    Marker marker = entry.getKey();
                    Place place = (Place) object;
                    if (place.getId() == objectId) marker.showInfoWindow();
                }
            }
        }
    }

    private void setMapListeners() {
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Object object = markersMap.get(marker);

                if (object instanceof Place) {
                    if (listener != null) {
                        listener.setOnInfoWindowClickListener((Place) object);
                    }
                }
            }
        });
    }

    private Polyline drawPath(List<LatLong> latLongs, int color, float zIndex, Polyline polyline) {
        if (googleMap != null && latLongs != null && !latLongs.isEmpty()) {
            PolylineOptions polyLineOptions = new PolylineOptions();
            polyLineOptions.width(getResources().getDimension(R.dimen._2dp));
            polyLineOptions.color(color);
            polyLineOptions.zIndex(zIndex);

            for (LatLong latLong : latLongs) {
                polyLineOptions.add(new LatLng(latLong.latitude(), latLong.longitude()));
            }

            if (polyline != null) polyline.remove();
            return googleMap.addPolyline(polyLineOptions);
        }

        return null;
    }

    private void drawSegmentPathUser(LatLng latLng) {
        PolylineOptions polyLineOptions = new PolylineOptions();
        polyLineOptions.width(getResources().getDimension(R.dimen._2dp));
        polyLineOptions.color(ContextCompat.getColor(getContext(), R.color.blue));
        polyLineOptions.zIndex(2f);

        if (polylineUserLastPath != null) {
            for (LatLng latLngOld : polylineUserLastPath.getPoints())
                polyLineOptions.add(latLngOld);
        }

        polyLineOptions.add(latLng);
        polylineUserLastPath = removePath(polylineUserLastPath);
        polylineUserLastPath = googleMap.addPolyline(polyLineOptions);
    }

    private void setCustomInfoWindow() {
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the custom contents of the InfoWindow
            @Override
            public View getInfoContents(final Marker marker) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.user_info_window, null);

                final Object object = markersMap.get(marker);

                if (object instanceof RouteStats) {
                    view.setLayoutParams(new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen._100dp), ViewGroup.LayoutParams.WRAP_CONTENT));
                    RouteStats routeStats = (RouteStats) object;
                    String text = getInfoWindowText(routeStats);
                    ((TextView) view.findViewById(R.id.tv_data)).setText(text);

                    return view;
                } else if (object instanceof Place) {
                    view.setLayoutParams(new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen._70dp), ViewGroup.LayoutParams.WRAP_CONTENT));
                    Place place = (Place) object;
                    String text = getInfoWindowText(place);
                    ((TextView) view.findViewById(R.id.tv_data)).setText(text);

                    return view;
                }

                return view;
            }
        });
    }

    private String getInfoWindowText(RouteStats routeStats) {
        return getTimeFormatted(routeStats.time()) + "\n" +
                getDistanceFormatted(routeStats.distance()) + "\n" +
                getSpeedFormatted(routeStats.speed(), true) + "\n" +
                routeStats.latLongs().size() + " waypoints";
    }

    private String getInfoWindowText(Place place) {
        return "Alt: " + getDistanceFormatted((long) place.getLatLong().altitude());
    }

    private String getDistanceFormatted(long distance) {
        if (distance < 1000) {
            return String.valueOf(distance) + " m";
        }

        return String.format(Locale.ENGLISH, "%.2f", distance / 1000f).replace(".", ",") + " km";
    }

    private String getSpeedFormatted(float speed, boolean isMetersPerSecond) {
        int speedConverted = Math.round(isMetersPerSecond ? speed * 3.6f : speed);
        return String.format("%d", speedConverted) + " km/h";
    }

    private String getTimeFormatted(long seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) (seconds - hours * 3600) / 60;
        int secs = (int) (seconds - hours * 3600 - minutes * 60);

//        return Observable.just(String.format("%01dh %02dm %02ds", hours, minutes, secs));
        return String.format("%01d:%02d:%02d", hours, minutes, secs);
    }
}

