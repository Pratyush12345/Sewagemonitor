package com.example.sewagemonitor;



public class UserActivity  {
    private double Wlevel;
    private double Latitude;
    private double Longitude;

    public UserActivity() {
    }

    public UserActivity(double wlevel, double latitude, double longitude) {
        this.Wlevel= wlevel;
        this.Latitude = latitude;
        this.Longitude = longitude;
    }

    public double getWlevel() {
        return Wlevel;
    }

    public void setWaterlevel(double wlevel) {
        this.Wlevel = wlevel;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        this.Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        this.Longitude = longitude;
    }
}

