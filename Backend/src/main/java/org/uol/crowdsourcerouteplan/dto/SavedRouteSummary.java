package org.uol.crowdsourcerouteplan.dto;

import java.time.LocalDateTime;

public class SavedRouteSummary {
    private Integer id;
    private Integer routeId;
    private String routeType;
    private String name;
    private String description;
    private LocalDateTime savedAt;

    public SavedRouteSummary() {
    }

    public SavedRouteSummary(Integer id, Integer routeId, String routeType, String name, String description, LocalDateTime savedAt) {
        this.id = id;
        this.routeId = routeId;
        this.routeType = routeType;
        this.name = name;
        this.description = description;
        this.savedAt = savedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
