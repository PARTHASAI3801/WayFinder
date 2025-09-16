package org.uol.crowdsourcerouteplan.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rp_routes")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "from_node_id")
    private Integer fromNodeId;
    @Column(name = "to_node_id")
    private Integer toNodeId;
    @Column(name = "path_json", columnDefinition = "TEXT")
    private String pathJSON;
    @Column(name = "description")
    private String description;
    @Column(name = "rating")
    private Double rating;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(Integer fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public Integer getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(Integer toNodeId) {
        this.toNodeId = toNodeId;
    }

    public String getPathJSON() {
        return pathJSON;
    }

    public void setPathJSON(String pathJSON) {
        this.pathJSON = pathJSON;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
