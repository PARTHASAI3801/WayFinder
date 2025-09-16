package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uol.crowdsourcerouteplan.model.Route;

import java.util.List;

@Repository
public interface RoutesRepo extends JpaRepository<Route, Integer> {

    List<Route> findAllByFromNodeIdAndToNodeId(Integer id, Integer id1);
}
