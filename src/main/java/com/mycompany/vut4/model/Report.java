/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.vut4.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author xbaratm00
 */
@Entity
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer hours;
    private String proportion;
    private Integer nbStudents;
    private String description;

    @ManyToOne
    @JsonManagedReference
    private Teacher teacher;

    @ManyToOne
    @JsonIgnore
    private Activity activity;

    @Override
    public String toString() {
        return "Report{" + "id=" + id + ", hours=" + hours + ", proportion=" + proportion + ", nbStudents=" + nbStudents + ", description=" + description + ", teacher=" + teacher + ", activity=" + activity + '}';
    }

    public Long getId() {
        return id;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public String getProportion() {
        return proportion;
    }

    public void setProportion(String proportion) {
        this.proportion = proportion;
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

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
