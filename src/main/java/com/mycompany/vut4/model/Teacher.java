/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vut4.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author xbaratm00
 */
@Entity
public class Teacher implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String login;

    @ManyToMany
    @JsonManagedReference
    @JoinTable(
            name = "teacher_courses",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "courses_id")
    )
    private List<Course> courses;

    @ManyToMany
    @JsonManagedReference
    @JoinTable(
            name = "teacher_tasks",
            joinColumns = @JoinColumn(name = "teachers_id"),
            inverseJoinColumns = @JoinColumn(name = "tasks_id")
    )
    private List<Task> tasks;


    @OneToMany(mappedBy = "teacher")
    @JsonBackReference
    private List<Report> reports;

    /*@Override
    public String toString() {
        return "Teacher{" + "id=" + id + ", login=" + login + ", courses=" + courses + ", tasks=" + tasks + ", reports=" + reports + '}';
    }*/

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

}

