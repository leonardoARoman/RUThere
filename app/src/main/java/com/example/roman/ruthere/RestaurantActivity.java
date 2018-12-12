package com.example.roman.ruthere;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.roman.ruthere.adapter.ReviewAdapter;
import com.example.roman.ruthere.pojo.Place;
import com.example.roman.ruthere.pojo.Restaurant;

import java.util.ArrayList;

public class RestaurantActivity extends AppCompatActivity {
    private TextView mName, mNumber, mAddress;
    private ImageView mOverallReview;
    private ImageButton mDirection;
    private Button mReservation;
    private ListView mReviews;

    private Place place;
    private Restaurant restaurant;

    private static final String TAG = RestaurantActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        mName = findViewById(R.id.name);
        mNumber = findViewById(R.id.number);
        mAddress = findViewById(R.id.address);
        mOverallReview = findViewById(R.id.overallreview);
        mDirection = findViewById(R.id.direction);
        mReservation = findViewById(R.id.reservation);
        mReviews = findViewById(R.id.reviews);
        mDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(RestaurantActivity.this,MapsActivity.class);
                ArrayList<Double> latLng=new ArrayList<>();
                latLng.add(place.getGeometry().getLocation().getLat());
                latLng.add(place.getGeometry().getLocation().getLng());
                intent.putExtra("location",latLng);
                startActivity(intent);
                Log.v("LATLNG",latLng.get(0)+","+latLng.get(1));
            }
        });
    }
    @Override
    public void onStart(){
        super.onStart();
        place = (Place) getIntent().getSerializableExtra("restaurant");
        restaurant = (Restaurant) getIntent().getSerializableExtra("yelpRestaurant");
        if (place!=null){
            mName.setText(place.getName().toString());
            mAddress.setText(place.getAddress().toString());
        }
        if (restaurant!=null){
            mName.setText(restaurant.getName().toString());
            mNumber.setText(restaurant.getPhone().toString());
            mAddress.setText(restaurant.getAddress().get(0)+", "+restaurant.getAddress().get(1));
            mName.setText(restaurant.getName().toString());
            mReviews.setAdapter(new ReviewAdapter(this,restaurant.getReviews()));
        }
    }
}
