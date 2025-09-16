package org.uol.crowdsourcerouteplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uol.crowdsourcerouteplan.model.UserActivityFeedRow;

import java.util.List;

public interface UserActivityLogRepo extends JpaRepository<UserActivityFeedRow, Integer> {
    @Query(value = """
        SELECT * FROM v_user_activity_feed
        WHERE userid = :userId
        ORDER BY time DESC
        """, nativeQuery = true)
    List<UserActivityFeedRow> findAllForUser(@Param("userId") int userId);
}

