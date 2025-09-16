package org.uol.crowdsourcerouteplan.dto;

import lombok.Data;

@Data
public class ExternalRouteLocationDTO {
    private String startLocation;
    private String endLocation;
    private String transportMode;
    private String routeType;




    public ExternalRouteLocationDTO() {
    }

    public ExternalRouteLocationDTO(String startLocation, String endLocation, String transportMode, String routeType) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.transportMode = transportMode;
        this.routeType = routeType;
    }


    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }
}
