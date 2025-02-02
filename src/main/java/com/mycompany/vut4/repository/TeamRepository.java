package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByActivityId(Long activityId);
    Team findByStudents_Id(Long studentId);
    @Query("SELECT t FROM Team t WHERE t.name = :teamName AND t.activity.id = :activityId")
    Team findTeamInDatabase(@Param("teamName") String teamName, @Param("activityId") Long activityId);
}
