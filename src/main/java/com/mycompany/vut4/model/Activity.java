/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vut4.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author xbaratm00
 */
@Entity
@Table (name = "activity")
public class Activity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public enum ActivityType {
        PROJECT_1,
        PROJECT_2,
        MIDTERM,
        FINAL_TERM,
        ASSIGNEMENT_1,
        ASSIGNEMENT_2
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private ActivityType type;

    private Date startDate;
    private Date endDate;
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Task> tasks;
    
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Group> groups;
    
    @ManyToOne
    @JsonBackReference
    private Course course;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Team> teams;

    @Override
    public String toString() {
        return "Activity{" + "id=" + id + ", type=" + type + ", tasks=" + tasks + ", groups_number=" + groups.size() + '}';
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Date getStartDate() {return startDate;}
    public void setStartDate(Date startdate){
        this.startDate = startdate;
    }

    public Date getEndDate() {return endDate;}
    public void setEndDate(Date enddate){
        this.endDate = enddate;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }


    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Long getId() {
        return id;
    }


    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }
}
