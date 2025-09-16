package org.uol.crowdsourcerouteplan.dto;

import org.uol.crowdsourcerouteplan.model.LocationPoint;

public class RouteSegmentsDTO {
    private LocationPoint from;
    private LocationPoint to;
    private String geometry;
    private double distance;
    private String duration;

    public RouteSegmentsDTO(LocationPoint from, LocationPoint to, String geometry,double distance, String duration) {
        this.from = from;
        this.to = to;
        this.geometry = geometry;
        this.distance = distance;
        this.duration = duration;
    }

    public RouteSegmentsDTO() {
    }




    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public LocationPoint getFrom() {
        return from;
    }

    public void setFrom(LocationPoint from) {
        this.from = from;
    }

    public LocationPoint getTo() {
        return to;
    }

    public void setTo(LocationPoint to) {
        this.to = to;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
