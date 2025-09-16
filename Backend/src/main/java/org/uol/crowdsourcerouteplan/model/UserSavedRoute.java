package org.uol.crowdsourcerouteplan.model;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rp_user_saved_routes")
@Data
public class UserSavedRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "route_id")
    private Integer routeId;
    @Column(name = "route_type")
    private String routeType;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "saved_at")
    @CreationTimestamp
    private LocalDateTime savedAt;

    public UserSavedRoute(Integer id, Integer userId, Integer routeId, String routeType, String name, String description, LocalDateTime savedAt) {
        this.id = id;
        this.userId = userId;
        this.routeId = routeId;
        this.routeType = routeType;
        this.name = name;
        this.description = description;
        this.savedAt = savedAt;
    }

    public UserSavedRoute() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
