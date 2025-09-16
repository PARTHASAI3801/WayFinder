package org.uol.crowdsourcerouteplan.dto;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponseDTO {
    public String description;
    public Double rating;
    public List<NodeDTO> nodes;

    public static class NodeDTO {
        public int id;
        public String name;
        public double lat;
        public double lng;

        public NodeDTO(int id, String name, double lat, double lng) {
            this.id = id;
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }


}
