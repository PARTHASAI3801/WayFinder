package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uol.crowdsourcerouteplan.model.ExternalViaRoute;

import java.util.Optional;

public interface ExternalViaRouteRepo extends JpaRepository<ExternalViaRoute, Integer> {
    Optional<ExternalViaRoute> findByStartCoordsAndEndCoordsAndViaCoordsAndMode(String startCoords, String endCoords, String viaCoordsJson, String mode);
}
