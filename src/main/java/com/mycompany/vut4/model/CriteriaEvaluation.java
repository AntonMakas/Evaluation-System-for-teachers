/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vut4.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author xbaratm00
 */
@Entity
public class CriteriaEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer points;
    private Integer teamSize;

    @ManyToOne
    @JsonManagedReference
    private Criteria criteria;

    @Override
    public String toString() {
        return "CriteriaEvaluation{" + "id=" + id + ", points=" + points + ", teamSize=" + teamSize + ", criteria=" + criteria + '}';
    }

    public Long getId() {
        return id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }
}
