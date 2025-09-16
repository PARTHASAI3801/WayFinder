package org.uol.crowdsourcerouteplan.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.uol.crowdsourcerouteplan.model.Edge;
import org.uol.crowdsourcerouteplan.model.Location;

import java.util.List;

@Repository
public class RouteRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RouteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Fetch location by name
    public Location findLocationByName(String name) {
        String sql = "SELECT id, name, latitude, longitude FROM rp_locations WHERE name = ?";
        List<Location> results = jdbcTemplate.query(sql, new Object[]{name}, locationRowMapper);
        return results.isEmpty() ? null : results.get(0);
    }

    // Fetch edges (neighbors) from a given location id
    public List<Edge> findEdgesFromLocation(int fromLocId) {
        String sql = "SELECT e.id, e.from_loc, e.to_loc, e.distance, " +
                "l1.id as from_id, l1.name as from_name, l1.latitude as from_lat, l1.longitude as from_lng, " +
                "l2.id as to_id, l2.name as to_name, l2.latitude as to_lat, l2.longitude as to_lng " +
                "FROM rp_edges e " +
                "JOIN rp_locations l1 ON e.from_loc = l1.id " +
                "JOIN rp_locations l2 ON e.to_loc = l2.id " +
                "WHERE e.from_loc = ?";

        return jdbcTemplate.query(sql, new Object[]{fromLocId}, edgeRowMapper);
    }

    private final RowMapper<Location> locationRowMapper = (rs, rowNum) -> new Location(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude")
    );

    private final RowMapper<Edge> edgeRowMapper = (rs, rowNum) -> {
        Location from = new Location(rs.getInt("from_id"), rs.getString("from_name"), rs.getDouble("from_lat"), rs.getDouble("from_lng"));
        Location to = new Location(rs.getInt("to_id"), rs.getString("to_name"), rs.getDouble("to_lat"), rs.getDouble("to_lng"));
        return new Edge(rs.getInt("id"), from, to, rs.getDouble("distance"));
    };
}
