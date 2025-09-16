package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uol.crowdsourcerouteplan.model.ExternalRoute;

import java.util.Optional;

public interface ExternalRouteRepo extends JpaRepository<ExternalRoute, Integer> {

    Optional<ExternalRoute> findByStartCoordinatesAndEndCoordinatesAndMode(String startCoordinates, String endCoordinates, String mode);

    boolean existsByStartCoordinatesAndEndCoordinatesAndMode(String startCoordinates, String endCoordinates, String mode);


}
