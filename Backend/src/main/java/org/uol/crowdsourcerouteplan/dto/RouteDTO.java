package org.uol.crowdsourcerouteplan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RouteDTO {
    public int fromNodeId;
    public int toNodeId;
    public List<Integer> routePath;
    public String description;
    public Double rating;

    public int getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(int fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public int getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(int toNodeId) {
        this.toNodeId = toNodeId;
    }

    public List<Integer> getRoutePath() {
        return routePath;
    }

    public void setRoutePath(List<Integer> routePath) {
        this.routePath = routePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
