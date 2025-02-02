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
@Table (name = "task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TaskType {
        EVALUATE_PROJECT_1,
        EVALUATE_PROJECT_2,
        EVALUATE_MIDTERM,
        EVALUATE_PLAN,
        EVALUATE_REPORT,
        EVALUATE_PROJECT_PROPOSAL
    }
    public enum TaskStatus {
        finished,
        doing,
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private TaskStatus status=TaskStatus.doing;
    @Column(name = "task_order")
    private Integer order;
    //private Integer mainTeacherIdx; // index of the main teacher in the teachers list, assuming that the teachers can't unregister from a task
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;

    private TaskType type;

    @ManyToOne
    @JsonManagedReference
    private Teacher mainTeacher;

    @ManyToMany(mappedBy = "tasks")
    @JsonBackReference // maybe back
    private List<Teacher> teachers;

    @ManyToOne
    @JsonManagedReference
    private Activity activity;

    @ManyToOne
    @JsonBackReference
    private Course course;

    /*@Override
    public String toString() {
        return "Task{" + "id=" + id + ", order=" + order + ", title=" + title + ", description=" + description + ", startDate=" + startDate + ", endDate=" + endDate + ", type=" + type + ", teachers=" + teachers + ", activity=" + activity + '}';
    }*/

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }


    // Getters and setters
    public Teacher getMainTeacher() {
        return mainTeacher;
    }

    public void setMainTeacher(Teacher mainTeacher) {
        this.mainTeacher = mainTeacher;
    }


    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }


    /*public Integer getMainTeacherIdx() {
        return mainTeacherIdx;
    }

    public void setMainTeacherIdx(Integer mainTeacherIdx) {
        this.mainTeacherIdx = mainTeacherIdx;
    }*/


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }


    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }


    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }


    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


    public Course getCourse() {return course;}
    public void setCourse(Course course) {this.course = course;}
}
