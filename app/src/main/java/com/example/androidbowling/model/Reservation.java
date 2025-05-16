package com.example.androidbowling.model;

public class Reservation {
    private String date;
    private String id;
    private String lane;

    public Reservation() {} // Firestore-nak kötelező

    public Reservation(String date, String id, String lane) {
        this.date = date;
        this.id = id;
        this.lane = lane;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String getLane() {
        return lane;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setId(String time) {
        this.id = time;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }
}
