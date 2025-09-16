package org.uol.crowdsourcerouteplan.model;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rp_external_route_stops")
@Data
public class ExternalRouteStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "start_coords")
    private String startCoords;
    @Column(name = "end_coords")
    private String endCoords;
    @Column(name = "via_Stops")
    private String viaStops;
    @Column(name = "transport_mode")
    private String mode;
    @Column(name = "polyline")
    private String polyline;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public ExternalRouteStop() {
    }

    public ExternalRouteStop(Integer id, String startCoords, String endCoords, String viaStops, String mode, String polyline, LocalDateTime createdAt) {
        this.id = id;
        this.startCoords = startCoords;
        this.endCoords = endCoords;
        this.viaStops = viaStops;
        this.mode = mode;
        this.polyline = polyline;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStartCoords() {
        return startCoords;
    }

    public void setStartCoords(String startCoords) {
        this.startCoords = startCoords;
    }

    public String getEndCoords() {
        return endCoords;
    }

    public void setEndCoords(String endCoords) {
        this.endCoords = endCoords;
    }

    public String getViaStops() {
        return viaStops;
    }

    public void setViaStops(String viaStops) {
        this.viaStops = viaStops;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
