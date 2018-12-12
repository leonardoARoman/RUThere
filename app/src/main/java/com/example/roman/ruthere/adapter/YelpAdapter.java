package com.example.roman.ruthere.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.roman.ruthere.MainActivity;
import com.example.roman.ruthere.MapsActivity;
import com.example.roman.ruthere.R;
import com.example.roman.ruthere.RestaurantActivity;
import com.example.roman.ruthere.YelpRecomendationActivity;
import com.example.roman.ruthere.pojo.Places;
import com.example.roman.ruthere.pojo.Restaurants;

import java.util.ArrayList;

public class YelpAdapter extends BaseAdapter{
    private YelpRecomendationActivity mainActivity;
    private Restaurants restaurants;

    public YelpAdapter(YelpRecomendationActivity mainActivity, Restaurants restaurants){
        this.mainActivity = mainActivity;
        this.restaurants = restaurants;
    }
    public void setList(Places places){
        this.restaurants = restaurants;
    }
    @Override
    public int getCount() {
        return restaurants.getRestaurants().size();
    }

    @Override
    public Object getItem(int position) {
        return restaurants.getRestaurants().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mainActivity);
        View view = inflater.inflate(R.layout.yelp_list_adapter,null);
        TextView mRestaurant = view.findViewById(R.id.yelpItem);
        TextView mRating = view.findViewById(R.id.yelpRating);
        mRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity.getBaseContext(),RestaurantActivity.class);
                intent.putExtra("yelpRestaurant",restaurants.getRestaurants().get(position));
                mainActivity.startActivity(intent);
            }
        });
        mRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity.getBaseContext(),RestaurantActivity.class);
                intent.putExtra("yelpRestaurant",restaurants.getRestaurants().get(position));
                mainActivity.startActivity(intent);
            }
        });
        /*
        ImageButton mDirections = view.findViewById(R.id.yelpImage);
        mDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mainActivity.getBaseContext(), MapsActivity.class);
                ArrayList<Double> latLng = new ArrayList();
                latLng.add(restaurants.getRestaurants().get(position).getCoordinates().getLatitude());
                latLng.add(restaurants.getRestaurants().get(position).getCoordinates().getLongitude());
                intent.putExtra("location",latLng);
                mainActivity.startActivity(intent);
            }
        });
        */
        mRestaurant.setText(restaurants.getRestaurants().get(position).getName());
        mRating.setText(restaurants.getRestaurants().get(position).getRating()+"");
        return view;
    }
}
