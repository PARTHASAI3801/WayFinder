package org.uol.crowdsourcerouteplan.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ContributeRouteRequest {
    private int userId;
    private String routeType;
    private String description;
    private List<List<Double>> coordinates;
    private String transportMode;

    public ContributeRouteRequest(int userId, String routeType, String description, List<List<Double>> coordinates, String transportMode) {
        this.userId = userId;
        this.routeType = routeType;
        this.description = description;
        this.coordinates = coordinates;
        this.transportMode = transportMode;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }




    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }
}
