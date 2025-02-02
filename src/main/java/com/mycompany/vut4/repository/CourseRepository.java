package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c JOIN c.teachers t WHERE t.id = :teacherId")
    List<Course> findCoursesByTeacherId(@Param("teacherId") Long teacherId);
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Course c WHERE c.name = :name")
    Boolean existsByName(@Param("name") String name);

}