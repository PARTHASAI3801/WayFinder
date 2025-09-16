package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uol.crowdsourcerouteplan.model.ContributeRoute;

import java.util.List;
import java.util.Optional;

public interface ContributeRouteRepo extends JpaRepository<ContributeRoute, Integer> {

    List<ContributeRoute> findByTransportMode(String transportMode);

    Optional<ContributeRoute> findByStartCoordinatesAndEndCoordinatesAndTransportMode(String startCoordinates, String endCoordinates, String transportMode);


    // ContributeRouteRepo.java
    @Query("SELECT c FROM ContributeRoute c WHERE c.transportMode = :mode AND c.routeType = :routeType")
    List<ContributeRoute> findByModeAndRouteType(@Param("mode") String mode, @Param("routeType") String routeType);

}
