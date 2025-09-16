package org.uol.crowdsourcerouteplan.dto;


import lombok.Data;
import org.uol.crowdsourcerouteplan.model.LocationPoint;

import java.util.List;

@Data
public class RoutePathDTO {
    private double totalDistance;
    private List<LocationPoint> path;

    public RoutePathDTO(double totalDistance, List<LocationPoint> path) {
        this.totalDistance = totalDistance;
        this.path = path;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public List<LocationPoint> getPath() {
        return path;
    }

    public void setPath(List<LocationPoint> path) {
        this.path = path;
    }
}
