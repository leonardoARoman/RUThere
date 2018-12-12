package com.example.roman.ruthere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.roman.ruthere.adapter.RestaurantListAdapter;
import com.example.roman.ruthere.api.PlacesAPI;
import com.example.roman.ruthere.api.YelpAPI;
import com.example.roman.ruthere.database.DatabaseManager;
import com.example.roman.ruthere.pojo.Place;
import com.example.roman.ruthere.pojo.Places;
import com.example.roman.ruthere.pojo.Restaurants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // WIDGET VARIABLES:
    private Button RESTAURANT_ACTIVITY_BUTTON, MAP_ACTIVITY_BUTTON, RECOMMENDATION_ACTIVITY_BUTTON;
    private ListView mRestaurantlist;
    // CONSTANT VARIABLES:
    private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_CODE = 1111;
    // CLASS FIELDS:
    private DatabaseManager db;
    String DEVICE_ID;
    private LocationManager locationManager;
    private Retrofit retrofit,retrofit_yelp;
    private Places places; // Defined class to store Place objects by aggregation
    private Restaurants restaurants;
    private RestaurantListAdapter adapter;
    private final String strDateFormat = "hh:mm:ss a";
    private static Date date;
    private static DateFormat dateFormat;
    private static boolean mLocationPermissionGranted = false;
    public static double lat, lon;
    private static Places mPlaces; // proxy to hold previous value from API call...
    private static int counter = 0;
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "message: onCreate called by " + Thread.currentThread().getName());
        setContentView(R.layout.activity_main);
        db = new DatabaseManager(this);
        DEVICE_ID = Secure.getString(getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
        mRestaurantlist = findViewById(R.id.restaurantlist);
        RESTAURANT_ACTIVITY_BUTTON = findViewById(R.id.restaurants);
        MAP_ACTIVITY_BUTTON = findViewById(R.id.map);
        RECOMMENDATION_ACTIVITY_BUTTON = findViewById(R.id.recomendations);
        dateFormat = new SimpleDateFormat(strDateFormat);
        date = new Date();
        retrofit = new Retrofit
                .Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit_yelp = new Retrofit
                .Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        getLocationPermission();
        getLocation();
        checkDataBase();
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    Log.d(TAG, "message: onLocationChanged.GPS_PROVIDER lat="+lat+" lon="+lon);

                    if (places!=null){
                        for (Place place:places.getPlaces()){
                            double x = place.getGeometry().getLocation().getLat();
                            double y = place.getGeometry().getLocation().getLng();
                            // IF CURRENT DISTANCE IS WITHIN 2m OF RESTAURANT CHECK IN AFTER COUNTER IS 5 (5 MINUTES)
                            if(checkDistance(x,y)){
                                counter++;
                                if (counter==5){
                                    // add to database (checkIn in database)
                                    place.setCheckedIn(true);
                                    String time = new SimpleDateFormat(strDateFormat).format(date);
                                    String calendar = new SimpleDateFormat("dd/MM/yyyy").format(date);
                                    db.checkIn(DEVICE_ID,time,calendar,x+"",y+"",place.getName(),place.getAddress());
                                    checkDataBase();
                                    Toast.makeText(MainActivity.this, place.getName()+" CHECKED IN!", Toast.LENGTH_LONG).show();
                                }
                                break;
                            }
                            if (place.isCheckedIn() && !checkDistance(x,y)){
                                counter = 0;
                                place.setCheckedIn(false);
                                // update database (user is not longer in restaurant) db.flag = false
                                db.checkOut(DEVICE_ID);
                                checkDataBase();
                                Toast.makeText(MainActivity.this, place.getName()+" CHECKED OUT!", Toast.LENGTH_LONG).show();
                            }
                            Log.d(TAG, "message: "+place.getName()+" lat="+place.getGeometry().getLocation().getLat()+" lon="+place.getGeometry().getLocation().getLng());
                        }
                    }
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
    private boolean checkDistance(double lat1, double lon1){
        float[] distance = new float[2];
        Location.distanceBetween(lat,lon,lat1,lon1,distance);
        Log.d(TAG, "message: distance = "+distance[0]);
        if (distance[0] <= 2){
            return true;
        }
        return false;
    }
    private void buttonListeners(){
        RESTAURANT_ACTIVITY_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "message: mRestaurants.Button clicked");
                // Make a RESTful API call to custom api using current locations
                PlacesAPI restaurants = retrofit.create(PlacesAPI.class);
                Call<Places> call = restaurants.getPlaces();
                call.enqueue(new Callback<Places>() {
                    @Override
                    public void onResponse(Call<Places> call, Response<Places> response) {
                        Places responseBody = response.body();
                        if (!responseBody.getPlaces().isEmpty()){
                            mRestaurantlist.setAdapter(new RestaurantListAdapter(MainActivity.this,responseBody));
                            //mPlaces.setPlaces(responseBody.getPlaces());
                            //mRestaurantlist.setAdapter(adapter);
                            places = responseBody;
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
        MAP_ACTIVITY_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "message: mMap.Button clicked");
                Intent intent = new Intent(MainActivity.this.getBaseContext(),MapsActivity.class);
                intent.putExtra("LatLng",lat+" "+lon);// current location
                intent.putExtra("places",places); // to create markers of all places
                startActivity(intent);
            }
        });
        // TO DO:
        RECOMMENDATION_ACTIVITY_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "message: mRecomendations.Button clicked");
                yelpCall();
            }
        });
    }

    private void yelpCall(){
        YelpAPI yelpAPI = retrofit_yelp.create(YelpAPI.class);
        Call<Restaurants> call = yelpAPI.getPlaces();
        call.enqueue(new Callback<Restaurants>() {
            @Override
            public void onResponse(Call<Restaurants> call, Response<Restaurants> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "message: Success "+response.toString());
                    restaurants = response.body();
                    if (!restaurants.getRestaurants().isEmpty()){
                        Intent intent = new Intent(MainActivity.this.getBaseContext(),YelpRecomendationActivity.class);
                        intent.putExtra("yelpList",restaurants);
                        startActivity(intent);
                    }
                }else {
                    Log.d(TAG, "message: not Successful "+response.code());

                }
            }

            @Override
            public void onFailure(Call<Restaurants> call, Throwable t) {
                Log.d(TAG, "message: Error "+t.getMessage());
            }
        });
        /*

        // This calls the python API using Volley class

        Log.d(TAG, "rest_response: yelpCall was called");
        String URL = "http://10.0.2.2:5000/restaurant/39.3898/-74.5240";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "rest_response: response "+response.toString());
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "rest_response: error "+error.toString());
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
        */
    }

    private void checkDataBase(){

        for (int i = 0; i < 585; i++){
            if (i<150)
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.5230139+"",-74.45850709999999+"","Moe's Southwest Grill","80232");
            if (i>=150 && i<200){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.523408+"",-74.458721+"","Gerlanda's Pizza","80232");
            }
            if (i>=200 && i<210){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.5233222+"",-74.4586443+"","Panera Bread","80232");
            }
            if (i>=210 && i<250){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.5229694+"",-74.45842189999999+"","Szechwan Ichiban","80232");
            }
            if (i>=250 && i<400){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.5225942+"",-74.4577283+"","Busch Faculty Dining Hall","80232");
            }
            if (i>=400 && i<475){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.52516749999999+"",-74.43942470000002+"","Henry's Diner","80232");
            }
            if (i>=475 && i<500){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.5258313+"",-74.4381446+"","Hoja Asian Fusion","80232");
            }
            if (i>=500 && i<530){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.525133+"",-74.4409001+"","QDOBA Mexican Eats","80232");
            }
            if (i>=530 && i<550){
                db.checkIn(i+"",(i+1%24)+"",(i+1%31)+"",40.525133+"",-74.4409001+"","Auntie Anne's","80232");
            }
        }

        List<String> list = db.getCurrentCrowd();

        for (String l: list){
            Log.d(TAG, "query: location "+l);
            String[] s = l.split(",");
            count+=Integer.parseInt(s[2]);
        }
        Log.d(TAG, "query: count =  "+count);

        int number = db.getPlaceCount("Moe's Southwest Grill");
        Log.d(TAG, "query: Moe's Southwest Grill count =  "+number);
    }
}
