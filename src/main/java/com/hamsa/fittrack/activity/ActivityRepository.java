package com.hamsa.fittrack.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Page<Activity> findByUserId(Long userId, Pageable pageable);

    Page<Activity> findByUserIdAndType(Long userId, ActivityType type, Pageable pageable);

    List<Activity> findByUserIdAndPerformedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    /** Only the timestamps are needed to calculate streaks, so we avoid loading full entities. */
    @Query("select a.performedAt from Activity a where a.user.id = :userId")
    List<LocalDateTime> findActivityTimesByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Activity a where a.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
