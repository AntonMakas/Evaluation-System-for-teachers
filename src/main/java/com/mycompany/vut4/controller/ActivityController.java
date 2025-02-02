package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.*;
import com.mycompany.vut4.model.Activity.ActivityType;
import com.mycompany.vut4.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class ActivityController {

    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CriteriaRepository theCriteriaRepository;
    @Autowired
    private CriteriaEvaluationRepository criteriaEvaluationRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private IndividualEvaluationRepository individualEvaluationRepository;
    @Autowired
    private TeamEvaluationRepository teamEvaluationRepository;
    @Autowired
    private ReportRepository reportRepository;


    /**
     * Handles POST requests to the "/activity/add" endpoint.
     * This method creates a new activity with the specified parameters,
     * associates it with selected groups, tasks, and course (if provided),
     * and saves it to the database.
     *
     * @param request     The HttpServletRequest object used to retrieve the referer header.
     * @param redirectAttributes The RedirectAttributes object used to pass flash attributes.
     * @param type        The type of the activity (e.g., TEAM, INDIVIDUAL).
     * @param name        The name of the activity.
     * @param StartDate   The start date of the activity.
     * @param EndDate     The end date of the activity.
     * @param TasksIds    The list of task IDs to associate with the activity.
     * @param groupIds    The list of group IDs to associate with the activity.
     * @param courseId    The ID of the course to associate with the activity.
     * @return            A redirect URL to the course page after successfully creating the activity.
     */
    @PostMapping("/activity/add")
    public String addActivity(HttpServletRequest request,
                              RedirectAttributes redirectAttributes,
                              @RequestParam(required = true) ActivityType type,
                              @RequestParam(required = true) String name,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date StartDate,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date EndDate,
                              @RequestParam(required = false) List<Long> TasksIds,
                              @RequestParam(required = false) List<Long> groupIds,
                              @RequestParam(required = false) Long courseId) {

        Activity activity = new Activity();
        activity.setType(type);
        activity.setName(name);
        activity.setStartDate(StartDate);
        activity.setEndDate(EndDate);

        if (groupIds != null && !groupIds.isEmpty()) {
            List<Group> groups = groupRepository.findAllById(groupIds);
            activity.setGroups(groups);
        }

        if (courseId != null) {
            Course course = courseRepository.findById(courseId).orElse(null);
            activity.setCourse(course);
        }

        if (TasksIds != null && !TasksIds.isEmpty()){
            List<Task> tasks = taskRepository.findAllById(TasksIds);
            activity.setTasks(tasks);
        }

        activityRepository.save(activity);

        redirectAttributes.addFlashAttribute("notificationMessage", "Activity created successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        String referer = request.getHeader("Referer");
        return "redirect:/course/" + courseId;
    }


    /**
     * Handles POST requests to the "/activity/edit/{id}" endpoint.
     * This method updates an existing activity's information (name, type, start/end dates)
     * based on the provided ID and form parameters.
     *
     * @param id          The ID of the activity to update.
     * @param courseId    The ID of the course the activity is associated with.
     * @param name        The new name of the activity.
     * @param type        The new type of the activity.
     * @param StartDate   The new start date of the activity.
     * @param EndDate     The new end date of the activity.
     * @return            A redirect URL to the course page after successfully updating the activity.
     */
    @PostMapping("/activity/edit/{id}")
    public String updateCourse(@PathVariable Long id,
                               @RequestParam Long courseId,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) ActivityType type,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date StartDate,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date EndDate) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Invalid course Id:"));
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));

        activity.setType(type);
        activity.setName(name);
        activity.setStartDate(StartDate);
        activity.setEndDate(EndDate);

        activityRepository.save(activity);
        return "redirect:/course/" + course.getId();
    }


    /**
     * Handles POST requests to the "/activity/delete/{id}" endpoint.
     * This method deletes an activity and all its associated data, including groups,
     * criteria, criteria evaluations, and tasks. The activity is then removed from the database.
     *
     * @param id               The ID of the activity to delete.
     * @param request         The HttpServletRequest object used to retrieve the referer header.
     * @param redirectAttributes The RedirectAttributes object used to pass flash attributes.
     * @return                A redirect URL to the previous page after successfully deleting the activity.
     */
    @PostMapping("/activity/delete/{id}")
    @Transactional
    public String deleteActivity(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<Team> teams = teamRepository.findByActivityId(id);
        for (Team team : teams) {
            List<Student> students = team.getStudents();
            for (Student student : students) {
                student.removeTeams(team);
            }
            team.setStudents(null);
        }
        List<Group> groups = groupRepository.findByActivityId(id);
        for (Group group : groups) {
            List<Criteria> criteriaList = theCriteriaRepository.findByGroupId(group.getId());
            for (Criteria criteria : criteriaList) {
                criteriaEvaluationRepository.deleteAllByCriteriaId(criteria.getId());
                individualEvaluationRepository.deleteAllByCriteriaId(criteria.getId());
                teamEvaluationRepository.deleteAllByCriteriaId(criteria.getId());
            }
            theCriteriaRepository.deleteByGroupId(group.getId());
        }
        groupRepository.deleteAll(groups);
        List<Report> reports = reportRepository.findByActivityId(id);
        List<Task> tasks = taskRepository.findByActivityId(id);
        taskRepository.deleteAll(tasks);
        reportRepository.deleteAll(reports);
        activityRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("notificationMessage", "Delete activity successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        String referer = request.getHeader("Referer");
        return "redirect:" + referer; // Redirect back to the previous page
    }


}

