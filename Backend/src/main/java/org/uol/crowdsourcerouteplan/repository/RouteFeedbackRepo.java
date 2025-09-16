package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uol.crowdsourcerouteplan.model.RouteFeedback;

import java.util.List;
import java.util.Optional;

public interface RouteFeedbackRepo extends JpaRepository<RouteFeedback, Integer> {

    List<RouteFeedback> findByRouteIdAndRouteType(int routeId, String routeType );

    Optional<RouteFeedback> findFirstByRouteIdAndUserId(int routeId, int userId);


    boolean existsByRouteIdAndRouteTypeAndUserId(int routeId, String externalvia, int userId);
}
