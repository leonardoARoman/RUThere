package com.example.roman.ruthere.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.roman.ruthere.MainActivity;
import com.example.roman.ruthere.R;
import com.example.roman.ruthere.RestaurantActivity;
import com.example.roman.ruthere.pojo.Places;

import java.util.List;

public class RestaurantListAdapter extends BaseAdapter {

    private MainActivity mainActivity;
    private Places places;

    public RestaurantListAdapter(MainActivity mainActivity, Places places){
        this.mainActivity = mainActivity;
        this.places = places;
    }
    public void setList(Places places){
        this.places = places;
    }
    @Override
    public int getCount() {
        return places.getPlaces().size();
    }

    @Override
    public Object getItem(int position) {
        return places.getPlaces().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mainActivity);
        View view = inflater.inflate(R.layout.restaurant_list_adapter,null);
        TextView mRestaurant = view.findViewById(R.id.restaurant);
        mRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity.getBaseContext(),RestaurantActivity.class);
                intent.putExtra("restaurant",places.getPlaces().get(position));
                mainActivity.startActivity(intent);
            }
        });
        ImageButton mDirections = view.findViewById(R.id.directions);
        mDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                // GO TO A MAP ACTIVITY AND DISPLAY ROUTE
                Intent intent = new Intent(context,MapsActivity.class);
                intent.putExtra("restaurant",list.get(position));
                context.startActivity(intent);
                */
            }
        });
        mRestaurant.setText(places.getPlaces().get(position).getName());
        return view;
    }

}
