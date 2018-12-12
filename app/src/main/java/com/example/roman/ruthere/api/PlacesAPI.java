package com.example.roman.ruthere.api;

import com.example.roman.ruthere.pojo.Places;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PlacesAPI {
    @GET("maps/api/place/nearbysearch/json?location=40.522,-74.453&radius=2000&type=restaurant&key=YOUR_API")
    Call<Places> getPlaces();
}
