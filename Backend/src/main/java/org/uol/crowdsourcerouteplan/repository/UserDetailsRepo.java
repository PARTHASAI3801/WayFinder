package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.uol.crowdsourcerouteplan.model.UserDetailsMgmt;

import java.util.Optional;

public interface UserDetailsRepo extends JpaRepository<UserDetailsMgmt, Integer> {

    Optional<UserDetailsMgmt> findByUserId(int userId);

    boolean existsByUserId(int userId);


}
