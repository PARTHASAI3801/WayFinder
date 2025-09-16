package org.uol.crowdsourcerouteplan.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteWithViaRequestDTO {
    private String startLocation;
    private List<String> viaStops;
    private String endLocation;
    private String transportMode;

    public RouteWithViaRequestDTO(String startLocation, List<String> viaStops, String endLocation, String transportMode) {
        this.startLocation = startLocation;
        this.viaStops = viaStops;
        this.endLocation = endLocation;
        this.transportMode = transportMode;
    }

    public RouteWithViaRequestDTO() {
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public List<String> getViaStops() {
        return viaStops;
    }

    public void setViaStops(List<String> viaStops) {
        this.viaStops = viaStops;
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
}
