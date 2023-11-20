package com.example.fitb;

public class LocationWrapper {
    private double latitude;
    private double longitude;

    // Empty constructor required for Firebase deserialization
    public LocationWrapper() {
    }

    public LocationWrapper(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

