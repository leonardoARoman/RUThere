package com.example.roman.ruthere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.roman.ruthere.adapter.RestaurantListAdapter;
import com.example.roman.ruthere.api.PlacesAPI;
import com.example.roman.ruthere.pojo.Place;
import com.example.roman.ruthere.pojo.Places;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // WIDGET VARIABLES:
    private Button mRestaurants, mMap, mRecomendations;
    private ListView mRestaurantlist;
    // CONSTANT VARIABLES:
    private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_CODE = 1111;
    // CLASS FIELDS:
    private LocationManager locationManager;
    private Retrofit retrofit;
    private Places places;
    private RestaurantListAdapter adapter;
    private final String strDateFormat = "hh:mm:ss a";
    private static Date date;
    private static DateFormat dateFormat;
    private static boolean mLocationPermissionGranted = false;
    private static double lat, lon;
    private static Places mPlaces; // proxy to hold previous value from API call...


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "message: onCreate called by " + Thread.currentThread().getName());
        setContentView(R.layout.activity_main);
        mRestaurantlist = findViewById(R.id.restaurantlist);
        mRestaurants = findViewById(R.id.restaurants);
        mMap = findViewById(R.id.map);
        mRecomendations = findViewById(R.id.recomendations);
        mPlaces = new Places();
        mPlaces.setPlaces(new ArrayList<Place>()); // create a arrayList for mPlace so it doesn't crash program
        adapter = new RestaurantListAdapter(this, mPlaces);
        retrofit = new Retrofit
                .Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        getLocationPermission();
        getLocation();
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "message: onResume called by " + Thread.currentThread().getName());
        // ONCE THE OTHER THREAD GETS PERMISSION SETMAP METHOD WILL BE INVOKED.
        if (mLocationPermissionGranted) {
            Log.d(TAG, "message: mLocationPermissionGranted " + mLocationPermissionGranted);
            getLocation();
        }
        buttonListeners();
    }
    // IF PERMISSION IS NOT YET GRANTED THIS METHOD WILL BE INVOKED AND PROMPTS USER TO GRANT PERMISSION
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "message: onRequestPermissionsResult called by " + Thread.currentThread().getName());
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "message: mLocationPermissionGranted " + Thread.currentThread().getName());
                }
            }
        }
    }
    // REQUEST USER PERMISSION FOR LOCATION ACCESS BY ENABLING FINE AND COARSE LOCATION ACCESS
    private void getLocationPermission() {
        Log.d(TAG, "message: getLocationPermission called by " + Thread.currentThread().getName());
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Log.d(TAG, "message: mLocationPermissionGranted " + Thread.currentThread().getName());
        } else {
            // PERMISSION IS NOT YET GRANTED. MAKE A REQUEST (calls onRequestPermissionsResult(...) method)
            ActivityCompat.requestPermissions(this, permissions, LOCATION_CODE);
        }
    }
    private void getLocation(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    Log.d(TAG, "message: onLocationChanged.GPS_PROVIDER lat="+lat+" lon="+lon);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }
    private void buttonListeners(){
        mRestaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "message: mRestaurants.Button clicked");
                // Make a RESTful API call to custom api using current locations
                PlacesAPI restaurants = retrofit.create(PlacesAPI.class);
                Call<Places> call = restaurants.getPlaces();
                call.enqueue(new Callback<Places>() {
                    @Override
                    public void onResponse(Call<Places> call, Response<Places> response) {
                        places = response.body();
                        if (!places.getPlaces().isEmpty()){
                            mRestaurantlist.setAdapter(new RestaurantListAdapter(MainActivity.this,places));
                            mPlaces.setPlaces(places.getPlaces());
                            mRestaurantlist.setAdapter(adapter);
                        }else {
                            Log.d(TAG, "message: API call no response ERROR!");
                        }
                    }

                    @Override
                    public void onFailure(Call<Places> call, Throwable t) {
                        Log.d(TAG, "message: call.enqueue.onFailure called");
                    }
                });
            }
        });
        // TO DO:
        mMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "message: mMap.Button clicked");
                Intent intent = new Intent(MainActivity.this.getBaseContext(),MapsActivity.class);
                intent.putExtra("LatLng",lat+" "+lon);// current location
                //intent.putExtra("Lat",ArrayList<>); // to create markers of all places
                //intent.putExtra("Lon",ArrayList<>); // to create markers of all places
                startActivity(intent);
            }
        });
        // TO DO:
        mRecomendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "message: mRecomendations.Button clicked");
            }
        });
    }

    // TO DO:
    private List<String> getRestaurants(int lat, int lon){
        // THIS WILL SEND A QUERY TO OUR RESTful API WITH LAT AND LON AS ARGUMENTS TO GET NEAREST RESTAURANTS

        // API CALL USING lat and lon
        List<String> list = new ArrayList<>();// this should get all restaurants from the API call
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        list.add("Numreo UNO");
        list.add("American Cut");
        list.add("Panda Express");
        return list;
    }
}
