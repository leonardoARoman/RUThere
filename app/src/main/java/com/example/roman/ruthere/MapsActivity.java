package com.example.roman.ruthere;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.roman.ruthere.database.DatabaseManager;
import com.example.roman.ruthere.directionhelpers.FetchURL;
import com.example.roman.ruthere.directionhelpers.TaskLoadedCallback;
import com.example.roman.ruthere.pojo.Location;
import com.example.roman.ruthere.pojo.Place;
import com.example.roman.ruthere.pojo.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;

import java.util.ArrayList;
import java.util.List;

public class  MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener , OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private Places places;
    private String[] latLng;
    private static final int DEFAULT_ZOOM = 14;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private Handler handlerAnimation;
    //direction API
    private LatLng currentLoacation;
    private GeoApiContext mGeoApiContext =null;
    ArrayList<PolylineData> mPolylineData=new ArrayList<>();
    ArrayList<Marker> markers;
    private  Marker mMarker;
    private DatabaseManager db;
    private Polyline currentPolyline;
    private static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "message: MapsActivity onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Direction Initialize
        currentLoacation = new LatLng(MainActivity.lat,MainActivity.lon);
        handlerAnimation = new Handler();
        markers = new ArrayList<>();
        db = new DatabaseManager(this);
        if(mGeoApiContext ==null){
            mGeoApiContext =new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "message: MapsActivity onMapReady called");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Places and coor get intent passed from main activity
        String coor = ((String) getIntent().getSerializableExtra("LatLng"));
        places = (Places) getIntent().getSerializableExtra("places");
        if(coor!=null){
            latLng=coor.split(" ");
            displayLocations();
            mMap.setOnMarkerClickListener(this);
        }
        // coordinates get intent passed from Red Icon ImageView activity
        ArrayList<Double> coordinates=(ArrayList<Double>) getIntent().getSerializableExtra("location");
        if(coordinates!=null){
            double  lat=coordinates.get(0);
            double  lon=coordinates.get(1);
            Log.v("LAT",lat+"");
            Log.v("LON",lon+"");
            LatLng selectedLocation =new LatLng(lat,lon);
            mMap.addMarker(new MarkerOptions().position(selectedLocation)).setTitle("Click to see route.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation,DEFAULT_ZOOM));
            currentLoacation=new LatLng(MainActivity.lat,MainActivity.lon);
            mMap.setOnMarkerClickListener(this);
        }
    }
    private void displayLocations(){
        Log.d(TAG, "message: MapsActivity displayLocations called");
        Log.d(TAG, "message: MapsActivity places "+places);
        if (places!=null){
            for (Place place: places.getPlaces()){
                Log.d(TAG, "message: MapsActivity places empty? "+places.getPlaces().isEmpty());
                Location location = place.getGeometry().getLocation();
                Marker marker =
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLat(),location.getLng()))
                        .title(place.getName()));
                markers.add(marker);
            }
        }
        Log.d(TAG, "message: markers "+markers.size());
        mMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(40.5418,-74.4562)).title("example"));
        currentLoacation = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
        mMap.addMarker(new MarkerOptions().position(currentLoacation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoacation,DEFAULT_ZOOM));
    }
    @Override
    public boolean onMarkerClick(final Marker marker) {
        count = db.getPlaceCount(marker.getTitle());
        LatLng dest=marker.getPosition();
        new FetchURL(MapsActivity.this).execute(getUrl(currentLoacation, dest, "driving"), "driving");
        handlerAnimation.post(new Runnable() {
            @Override
            public void run() {
                markers.get(markers.indexOf(marker)).setTitle(marker.getTitle()+": "+count+" People");
                //mMarker.setTitle(count+"");
            }
        });
        return false;
    }
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    private int getCount(){
        List<String> list = db.getCurrentCrowd();
        int count = 0;
        for (String l: list){
            Log.d(TAG, "message: location "+l);
            String[] s = l.split(",");
            count+=Integer.parseInt(s[2]);
        }
        Log.d(TAG, "message: count =  "+count);
        return count;
    }
}
