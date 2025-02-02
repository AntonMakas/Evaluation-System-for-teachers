package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Course;
import com.mycompany.vut4.model.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCourseId(Long courseId);
    List<Task> findByActivityId(Long activityId);
    @Query("SELECT c FROM Task c JOIN c.teachers t WHERE t.id = :teacherId AND c.status = :status")
    List<Task> searchDoingTaskByTeacherId(@Param("teacherId") Long teacherId, @Param("status") Task.TaskStatus status);

}