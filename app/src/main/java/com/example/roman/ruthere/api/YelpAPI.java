package com.example.roman.ruthere.api;

import com.example.roman.ruthere.pojo.Restaurant;
import com.example.roman.ruthere.pojo.Restaurants;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface YelpAPI {
    @GET("restaurant/40.522/-74.453")
    Call<Restaurants> getPlaces();
}
