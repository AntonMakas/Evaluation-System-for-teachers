package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Group;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByActivityId(Long activityId);
}
