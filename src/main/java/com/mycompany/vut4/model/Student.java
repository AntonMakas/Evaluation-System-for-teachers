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
/*import lombook.Data;*/

/**
 *
 * @author xbaratm00
 */
@Entity
@Table(name = "student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;

    @OneToMany(mappedBy="student")
    @JsonBackReference
    private List<IndividualEvaluation> individualEvaluations;

    @ManyToMany
    @JsonManagedReference
    private List<Course> courses;
    @ManyToMany
    @JsonManagedReference
    private List<Team> teams;

    @Override
    public String toString() {
        return "Student{" + "id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", individualEvaluations=" + individualEvaluations + ", courses=" + courses + ", teams_number=" + teams.size() + '}';
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<IndividualEvaluation> getIndividualEvaluations() {
        return individualEvaluations;
    }

    public void setIndividualEvaluations(List<IndividualEvaluation> individualEvaluations) {
        this.individualEvaluations = individualEvaluations;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
    public void addCourses(List<Course> newcourses) {
        for(Course course: newcourses){
            if(!this.courses.contains(course)) {
                this.courses.add(course);
            }
        }

    }

    public List<Team> getTeams() {
        return teams;
    }

    public void addTeams(Team team) {
        this.teams.add(team);
    }
    public void removeTeams(Team team) {
        this.teams.remove(team);
    }
}
