package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uol.crowdsourcerouteplan.model.Edge;
import org.uol.crowdsourcerouteplan.model.Location;

import java.util.Optional;

public interface EdgeRepo extends JpaRepository<Edge, Integer> {


    boolean existsByFromLocationAndToLocation(Optional<Location> fromLocation, Optional<Location> toLocation);
}
