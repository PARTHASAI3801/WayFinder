package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uol.crowdsourcerouteplan.model.UserSavedRoute;

import java.util.List;
import java.util.Optional;

public interface UserSavedRouteRepo extends JpaRepository<UserSavedRoute, Integer> {





    Optional<UserSavedRoute> findFirstByUserIdAndRouteIdAndRouteType(Integer userId, Integer routeId, String routeType);

    List<UserSavedRoute> findAllByUserId(Integer userId);
}
