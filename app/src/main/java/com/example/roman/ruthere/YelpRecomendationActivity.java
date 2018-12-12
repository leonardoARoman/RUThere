package com.example.roman.ruthere;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.roman.ruthere.adapter.YelpAdapter;
import com.example.roman.ruthere.pojo.Restaurants;

public class YelpRecomendationActivity extends AppCompatActivity {
    private ListView mYelpRecommendation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelp_recomendation);
        mYelpRecommendation = findViewById(R.id.yelpRecommendation);
        Restaurants restaurants = (Restaurants) getIntent().getSerializableExtra("yelpList");
        if (restaurants!=null){
            mYelpRecommendation.setAdapter(new YelpAdapter(YelpRecomendationActivity.this,restaurants));
        }
    }
}
