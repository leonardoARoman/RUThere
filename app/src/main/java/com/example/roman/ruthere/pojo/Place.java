package com.example.roman.ruthere.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Place implements Serializable {
    private String name;
    @SerializedName("vicinity")
    private String address;
    private Geometry geometry;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}

