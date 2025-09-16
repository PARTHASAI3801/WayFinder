package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uol.crowdsourcerouteplan.model.ExternalRouteStop;

import java.util.Optional;

public interface ExternalRouteStopsRepo extends JpaRepository<ExternalRouteStop,Integer> {

    Optional<ExternalRouteStop> findByStartCoordsAndEndCoordsAndModeAndViaStops(String startCoords, String endCoords, String mode, String viaStops);

    boolean existsByStartCoordsAndEndCoordsAndModeAndViaStops(String startCoords, String endCoords, String mode, String viaStops);






}
