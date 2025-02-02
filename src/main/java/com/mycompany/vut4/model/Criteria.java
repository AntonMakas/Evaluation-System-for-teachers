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
public class Criteria implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum CriteriaType {
        TEAM,
        INDIVIDUAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private CriteriaType type;

    @ManyToOne
    @JsonManagedReference
    private Group group;

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<CriteriaEvaluation> criteriaEvaluations;

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<TeamEvaluation> teamEvaluations;

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<IndividualEvaluation> individualEvaluations;

    @Override
    public String toString() {
        return "Criteria{" + "id=" + id + ", name=" + name + ", type=" + type + ", group=" + group + ", criteriaEvaluations=" + criteriaEvaluations + ", teamEvaluations=" + teamEvaluations + ", individualEvaluations=" + individualEvaluations + '}';
    }

    public List<CriteriaEvaluation> getCriteriaEvaluations() {
        return criteriaEvaluations;
    }

    public void setCriteriaEvaluations(List<CriteriaEvaluation> criteriaEvaluations) {
        this.criteriaEvaluations = criteriaEvaluations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<TeamEvaluation> getTeamEvaluations() {
        return teamEvaluations;
    }

    public void setTeamEvaluations(List<TeamEvaluation> teamEvaluations) {
        this.teamEvaluations = teamEvaluations;
    }

    public List<IndividualEvaluation> getIndividualEvaluations() {
        return individualEvaluations;
    }

    public void setIndividualEvaluations(List<IndividualEvaluation> individualEvaluations) {
        this.individualEvaluations = individualEvaluations;
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

    public CriteriaType getType() {
        return type;
    }

    public void setType(CriteriaType type) {
        this.type = type;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
