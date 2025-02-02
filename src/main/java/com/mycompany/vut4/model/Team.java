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
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private Integer leaderIdx;
    private Integer nbStudents;
    private String description;

    @ManyToMany
    @JsonBackReference
    private List<Student> students;

    @ManyToOne
    @JsonManagedReference
    private Activity activity;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<TeamEvaluation> teamEvaluations;

    @Override
    public String toString() {
        return "Team{" + "id=" + id + ", name=" + name + ", leaderIdx=" + leaderIdx + ", nbStudents=" + nbStudents + ", description=" + description + ", students_number=" + students.size() + ", activity=" + activity + ", teamEvaluations=" + teamEvaluations + '}';
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

    public Integer getLeaderIdx() {
        return leaderIdx;
    }

    public void setLeaderIdx(Integer leaderIdx) {
        this.leaderIdx = leaderIdx;
    }

    public Integer getNbStudents() {
        return nbStudents;
    }

    public void setNbStudents(Integer nbStudents) {
        this.nbStudents = nbStudents;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public List<TeamEvaluation> getTeamEvaluations() {
        return teamEvaluations;
    }

    public void setTeamEvaluations(List<TeamEvaluation> teamEvaluations) {
        this.teamEvaluations = teamEvaluations;
    }

}
