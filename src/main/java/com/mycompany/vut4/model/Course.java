/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vut4.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author xbaratm00
 */
@Entity
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_seq")
    @SequenceGenerator(name = "course_seq", sequenceName = "course_id_seq", allocationSize = 1)
    private Long id;
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "courses")
    @JsonBackReference
    @JsonIgnore
    private List<Teacher> teachers;

    @OneToMany(mappedBy = "course")
    @JsonManagedReference
    private List<Activity> activities;

    @ManyToMany(mappedBy = "courses")
    @JsonBackReference
    private List<Student> students;

    @Override
    public String toString() {
        return "Course{" + "id=" + id + ", name=" + name + ", teachers=" + teachers + ", students_number=" + students.size() + '}';
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
