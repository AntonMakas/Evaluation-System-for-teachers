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
public class IndividualEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer points;
    private String feedback;

    @ManyToOne
    @JsonManagedReference
    private Criteria criteria;

    @ManyToOne
    @JsonManagedReference
    private Student student;

    @Override
    public String toString() {
        return "IndividualEvaluation{" + "id=" + id + ", points=" + points + ", feedback=" + feedback + ", criteria=" + criteria + ", student=" + student + '}';
    }

    public Long getId() {
        return id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
