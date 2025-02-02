package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Criteria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Long> {
    List<Criteria> findByGroupId(Long groupId);
    @Modifying
    @Query("DELETE FROM Criteria e WHERE e.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
