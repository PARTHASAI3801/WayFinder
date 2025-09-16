package org.uol.crowdsourcerouteplan.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationPoint {
    private int id;
    private String name;
    private double latitude;
    private double longitude;
    private double distanceFromPrevious;

    public LocationPoint(int id, String name, double latitude, double longitude, double distanceFromPrevious) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromPrevious = distanceFromPrevious;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getDistanceFromPrevious() {
        return distanceFromPrevious;
    }

    public void setDistanceFromPrevious(double distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;
    }
}
