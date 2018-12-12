package com.example.roman.ruthere.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.roman.ruthere.R;
import com.example.roman.ruthere.RestaurantActivity;
import com.example.roman.ruthere.pojo.Reviews;

import java.util.ArrayList;

public class ReviewAdapter extends BaseAdapter {
    RestaurantActivity restaurantActivity;
    private ArrayList<Reviews> reviews;

    public ReviewAdapter(RestaurantActivity restaurantActivity, ArrayList<Reviews> reviews) {
        this.restaurantActivity = restaurantActivity;
        this.reviews = reviews;
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public Object getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(restaurantActivity);
        View view = inflater.inflate(R.layout.review_list_adapter,null);
        TextView mUserName = view.findViewById(R.id.userName);
        TextView mDate = view.findViewById(R.id.date);
        TextView mUserComment = view.findViewById(R.id.userComment);
        mUserName.setText(reviews.get(position).getUser());
        mDate.setText(reviews.get(position).getDate());
        mUserComment.setText(reviews.get(position).getReview());
        return view;
    }
}
