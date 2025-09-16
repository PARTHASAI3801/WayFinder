package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uol.crowdsourcerouteplan.model.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    boolean existsByUsername(String uname);

    Optional<User> findByUsername(String uname);
}
