package org.uol.crowdsourcerouteplan.dto;

import java.util.List;

public class RoutePathWithSegmentsDTO {
    private double totalDistance;
    private String duration;
    private List<RouteSegmentsDTO> segments;

    public RoutePathWithSegmentsDTO(double totalDistance,String duration, List<RouteSegmentsDTO> segments) {
        this.totalDistance = totalDistance;
        this.duration = duration;
        this.segments = segments;
    }

    public RoutePathWithSegmentsDTO() {
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public List<RouteSegmentsDTO> getSegments() {
        return segments;
    }

    public void setSegments(List<RouteSegmentsDTO> segments) {
        this.segments = segments;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
