package org.uol.crowdsourcerouteplan.model;

public class RouteStep {
    private Location location;
    private double distanceFromPrevious;

    public RouteStep(Location location, double distanceFromPrevious) {
        this.location = location;
        this.distanceFromPrevious = distanceFromPrevious;
    }

    public RouteStep() {
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getDistanceFromPrevious() {
        return distanceFromPrevious;
    }

    public void setDistanceFromPrevious(double distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;
    }
}
