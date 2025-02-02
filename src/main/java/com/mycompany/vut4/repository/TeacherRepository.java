package com.mycompany.vut4.repository;

import com.mycompany.vut4.model.Teacher;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Teacher findByLogin(String login);
}
