package org.uol.crowdsourcerouteplan.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.uol.crowdsourcerouteplan.model.Location;

import java.util.Optional;

public interface LocationRepo extends JpaRepository<Location, Integer> {



    boolean existsByName(String name);

    Optional<Location> findByNameIgnoreCase(String name);



}
