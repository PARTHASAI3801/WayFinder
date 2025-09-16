package org.uol.crowdsourcerouteplan.dto;

public class SavedRouteRequest {
    private Integer userId;
    private Integer routeId;
    private String routeType;
    private String name;
    private String description;

    public SavedRouteRequest(Integer userId, Integer routeId, String routeType, String name, String description) {
        this.userId = userId;
        this.routeId = routeId;
        this.routeType = routeType;
        this.name = name;
        this.description = description;
    }

    public SavedRouteRequest() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
