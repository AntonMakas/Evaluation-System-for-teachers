package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.IndividualEvaluation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndividualEvaluationRepository extends JpaRepository<IndividualEvaluation, Long> {
    List<IndividualEvaluation> findByCriteriaId(Long criteriaId);
    IndividualEvaluation findByStudentIdAndCriteriaId(Long studentId, Long criteriaId);
    @Modifying
    @Query("UPDATE IndividualEvaluation e SET e.criteria = NULL WHERE e.criteria.id = :criteriaId")
    void clearCriteriaReferences(@Param("criteriaId") Long criteriaId);
    void deleteAllByCriteriaId(Long criteriaId);

}
