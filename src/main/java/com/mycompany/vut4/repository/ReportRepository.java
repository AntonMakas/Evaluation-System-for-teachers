package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByActivityId(Long activityId);
}
