package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Course;
import com.mycompany.vut4.model.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByCoursesId(Long courseId);
    @Query("SELECT s FROM Student s where s.email = :studentEmail")
    Student findStudentByEmail(@Param("studentEmail") String studentEmail);
}
