package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Team;
import com.mycompany.vut4.model.TeamEvaluation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamEvaluationRepository extends JpaRepository<TeamEvaluation, Long> {
    List<TeamEvaluation> findByTeamId(Long teamId);
    List<TeamEvaluation> findByCriteriaId(Long criterionId);
    TeamEvaluation findByTeamIdAndCriteriaId(Long teamId, Long criterionId);
    @Modifying
    @Query("DELETE FROM IndividualEvaluation e WHERE e.criteria.id = :criteriaId")
    void deleteAllByCriteriaId(Long criteriaId);
}
