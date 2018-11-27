package com.example.roman.ruthere;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.roman.ruthere.pojo.Place;

public class RestaurantActivity extends AppCompatActivity {
    private TextView mName, mNumber, mAddress;
    private ImageView mOverallReview;
    private ImageButton mDirection;
    private Button mReservation;
    private ListView mReviews;

    private Place place;

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
    }
    @Override
    public void onStart(){
        super.onStart();
        place = (Place) getIntent().getSerializableExtra("restaurant");
        mName.setText(place.getName().toString());
        Log.d(TAG, "message: place.getAddress "+place.getAddress());

        mAddress.setText(place.getAddress().toString());
    }
}
//Mobile Application class final project. This in an Android RESTful application which gets nearest restaurants using coordinates from mobile current location. The application uses the lat and lng coor to make an API GET request to GOOGLE Places and uses that response to make other API casll to YELP and (Intagram or Twitter)