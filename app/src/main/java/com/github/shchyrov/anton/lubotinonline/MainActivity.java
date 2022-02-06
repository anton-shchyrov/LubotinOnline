package com.github.shchyrov.anton.lubotinonline;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends AppCompatActivity {
    static class PointInfo {
        public final double latitude;
        public final double longitude;
        public final String url;

        PointInfo(double latitude, double longitude, String url) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.url = url;
        }
    }
    private static PointInfo[] points = new PointInfo[] {
        new PointInfo(49.948333, 35.929444, "https://uk.wikipedia.org/wiki/%D0%9B%D1%8E%D0%B1%D0%BE%D1%82%D0%B8%D0%BD"),
        new PointInfo(49.974444, 35.9475, "https://uk.wikipedia.org/wiki/%D0%92%D0%BE%D0%B7%D0%BD%D0%B5%D1%81%D0%B5%D0%BD%D1%81%D1%8C%D0%BA%D0%B0_%D1%86%D0%B5%D1%80%D0%BA%D0%B2%D0%B0_(%D0%9B%D1%8E%D0%B1%D0%BE%D1%82%D0%B8%D0%BD)"),
        new PointInfo(49.925, 35.952778, "https://uk.wikipedia.org/wiki/%D0%9F%D0%B0%D0%BB%D0%B0%D1%86_%D0%BA%D0%BD%D1%8F%D0%B7%D1%96%D0%B2_%D0%A1%D0%B2%D1%8F%D1%82%D0%BE%D0%BF%D0%BE%D0%BB%D0%BA-%D0%9C%D0%B8%D1%80%D1%81%D1%8C%D0%BA%D0%B8%D1%85_%D1%83_%D0%9B%D1%8E%D0%B1%D0%BE%D1%82%D0%B8%D0%BD%D1%96"),
        new PointInfo(49.944722, 35.926944, "https://uk.wikipedia.org/wiki/%D0%9B%D1%8E%D0%B1%D0%BE%D1%82%D0%B8%D0%BD%D1%81%D1%8C%D0%BA%D0%B8%D0%B9_%D0%BC%D1%96%D1%81%D1%8C%D0%BA%D0%B8%D0%B9_%D0%BA%D1%80%D0%B0%D1%94%D0%B7%D0%BD%D0%B0%D0%B2%D1%87%D0%B8%D0%B9_%D0%BC%D1%83%D0%B7%D0%B5%D0%B9")
    };

    private MapView map;
    private IMapController mapController;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private MyLocationNewOverlay gpsOverlay;
    private FloatingActionButton locationButton;

    private void changeFollowLocation(boolean val) {
        int color = (val) ? Color.BLUE : Color.BLACK;
        locationButton.setImageTintList(ColorStateList.valueOf(color));
    }

    private void createGPSOverlay() {
        gpsOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map) {
            @Override
            public void enableFollowLocation() {
                super.enableFollowLocation();
                changeFollowLocation(isFollowLocationEnabled());
            }

            @Override
            public void disableFollowLocation() {
                super.disableFollowLocation();
                changeFollowLocation(isFollowLocationEnabled());
            }
        };
        gpsOverlay.enableMyLocation();
        setFollowLocation(null);
        map.getOverlayManager().add(gpsOverlay);
    }

    private void setFollowLocation(SharedPreferences sharedPref) {
        if (sharedPref == null)
            sharedPref = getPreferences();
        boolean followLocation = sharedPref.getBoolean(getString(R.string.setting_follow_location), true);
        if (followLocation)
            gpsOverlay.enableFollowLocation();
        else
            gpsOverlay.disableFollowLocation();
    }

    private void tryCreateGPSOverlay() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else
            createGPSOverlay();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.zoom_in);
        fab.setOnClickListener(btn -> mapController.zoomIn());

        fab = findViewById(R.id.zoom_out);
        fab.setOnClickListener(btn -> mapController.zoomOut());

        locationButton = findViewById(R.id.location);
        locationButton.setOnClickListener(btn -> {
            if (gpsOverlay != null)
                gpsOverlay.enableFollowLocation();
            else
                tryCreateGPSOverlay();
        });

        requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (result) -> {
                Boolean granted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if (granted != null && granted)
                    createGPSOverlay();
            });

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = findViewById(R.id.map);
        map.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapController = map.getController();

        tryCreateGPSOverlay();

//        CompassOverlay compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
//        compassOverlay.enableCompass();
//        map.getOverlays().add(compassOverlay);

        for (PointInfo point : points)
            addMarker(point);
        loadSettings();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveSettings();
    }

    private SharedPreferences getPreferences() {
        return getPreferences(Context.MODE_PRIVATE);
    }

    private double readSettingDouble(SharedPreferences sharedPref, int nameRes, int defNameRes) {
        float defVal = Float.parseFloat(getResources().getString(defNameRes));
        return sharedPref.getFloat(getString(nameRes), defVal);
    }

    private void writeSetting(SharedPreferences.Editor editor, int nameRes, double value) {
        editor.putFloat(getString(nameRes), (float) value);
    }

    private void loadSettings() {
        SharedPreferences sharedPref = getPreferences();
        double lat = readSettingDouble(sharedPref, R.string.setting_latitude, R.string.def_latitude);
        double lon = readSettingDouble(sharedPref, R.string.setting_longitude, R.string.def_longitude);
        mapController.setCenter(new GeoPoint(lat, lon));

        double zoom = readSettingDouble(sharedPref, R.string.setting_zoom, R.string.def_zoom);
        mapController.setZoom(zoom);

        if (gpsOverlay != null)
            setFollowLocation(sharedPref);
    }

    private void saveSettings() {
        SharedPreferences sharedPref = getPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        IGeoPoint center = map.getMapCenter();
        writeSetting(editor, R.string.setting_latitude, center.getLatitude());
        writeSetting(editor, R.string.setting_longitude, center.getLongitude());
        writeSetting(editor, R.string.setting_zoom, map.getZoomLevelDouble());
        editor.putBoolean(getString(R.string.setting_follow_location), gpsOverlay.isFollowLocationEnabled());
        editor.apply();
    }

    public Marker addMarker(PointInfo info) {
        Marker res = new Marker(map);
        res.setPosition(new GeoPoint(info.latitude, info.longitude));
        map.getOverlays().add(res);
        res.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        int size = convertDpToPixels(getContext(), 30);
//        Drawable drawable = AggregateIconBuilder.createDrawable(size, 5, 10, 15, false, false);
//        marker.setIcon(drawable);
//        res.setTitle(title);
//        res.setSnippet(subTitle);
//        marker.setInfoWindow(new CustomMarkerInfoWindow(osm));
        res.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
        res.setId(info.url);
        res.setOnMarkerClickListener((marker, map) -> {
            Intent intent = new Intent(this, WikiActivity.class);
            intent.putExtra(WikiActivity.EXTRA_URL, marker.getId());
            startActivity(intent);
            return true;
        });
        return res;
    }

    private static int convertDpToPixels(Context context, int dp) {
        return Math.round (dp * context.getResources().getDisplayMetrics().density);
    }

}