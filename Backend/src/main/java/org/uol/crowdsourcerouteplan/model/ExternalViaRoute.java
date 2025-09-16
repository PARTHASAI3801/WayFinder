package org.uol.crowdsourcerouteplan.model;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rp_external_via_route")
@Data
public class ExternalViaRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "start_coords")
    private String startCoords;
    @Column(name = "end_coords")
    private String endCoords;
    @Column(name = "via_coords")
    private String viaCoords;
    @Column(name = "mode")
    private String mode;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public ExternalViaRoute() {
    }

    public ExternalViaRoute(int id, String startCoords, String endCoords, String viaCoords, String mode, LocalDateTime createdAt) {
        this.id = id;
        this.startCoords = startCoords;
        this.endCoords = endCoords;
        this.viaCoords = viaCoords;
        this.mode = mode;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getViaCoords() {
        return viaCoords;
    }

    public void setViaCoords(String viaCoords) {
        this.viaCoords = viaCoords;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
