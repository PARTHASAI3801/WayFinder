package org.uol.crowdsourcerouteplan.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserContributedRouteDTO {
    private List<List<Double>> coordinates;
    private String username;
    private String description;

    public UserContributedRouteDTO() {
    }

    public UserContributedRouteDTO(List<List<Double>> coordinates, String username, String description) {
        this.coordinates = coordinates;
        this.username = username;
        this.description = description;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
