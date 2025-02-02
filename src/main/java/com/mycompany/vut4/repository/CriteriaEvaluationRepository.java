package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.CriteriaEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaEvaluationRepository extends JpaRepository<CriteriaEvaluation, Long> {
    CriteriaEvaluation findByCriteriaId(Long criterionId);
    @Modifying
    @Query("DELETE FROM CriteriaEvaluation e WHERE e.criteria.id = :criteriaId")
    void deleteAllByCriteriaId(Long criteriaId);

}
