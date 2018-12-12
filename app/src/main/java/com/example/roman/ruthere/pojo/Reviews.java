package com.example.roman.ruthere.pojo;

import java.io.Serializable;

public class Reviews implements Serializable {
    private String date;
    private String review;
    private String user;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
