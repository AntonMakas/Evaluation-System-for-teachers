package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.Activity;
import com.mycompany.vut4.model.Course;
import com.mycompany.vut4.model.Task;
import com.mycompany.vut4.model.Teacher;
import com.mycompany.vut4.repository.ActivityRepository;
import com.mycompany.vut4.repository.CourseRepository;
import com.mycompany.vut4.repository.TaskRepository;
import com.mycompany.vut4.repository.TeacherRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;

@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private CourseRepository courseRepository;


    /**
     * Handles POST requests to add a new task.
     * Creates a new task with the specified details and associates it with an activity or course if provided.
     *
     * @param request             The HttpServletRequest object used to retrieve the referring page.
     * @param redirectAttributes  Used to pass flash attributes for notification messages.
     * @param title               The title of the task (required).
     * @param description         The description of the task (optional).
     * @param taskOrder           The display order of the task (optional).
     * @param type                The type of the task (required).
     * @param endDate             The end date of the task (optional).
     * @param startDate           The start date of the task (optional).
     * @param activityId          The ID of the associated activity (optional).
     * @param courseId            The ID of the associated course (optional).
     *
     * @return Redirects to the referring page after successfully creating the task.
     *
     * @throws RuntimeException if the provided task type is invalid.
     */
    @PostMapping("/task/add")
    public String addTask(HttpServletRequest request,
                          RedirectAttributes redirectAttributes,
                          @RequestParam(required = true) String title,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) Integer taskOrder,
                          @RequestParam(required = true)  String type,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date endDate,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date startDate,
                          @RequestParam(required = false) Long activityId,
                          @RequestParam(required = false) Long courseId) {

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setOrder(taskOrder);
        task.setEndDate(endDate);
        task.setStartDate(startDate);

        try {
            Task.TaskType taskType = Task.TaskType.valueOf(type);
            task.setType(taskType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task type: " + type);
        }

        if (activityId != null) {
            Activity activity = activityRepository.findById(activityId).orElse(null);
            task.setActivity(activity);
        }
        if (courseId != null) {
            Course course = courseRepository.findById(courseId).orElse(null);
            task.setCourse(course);
        }

        taskRepository.save(task);

        redirectAttributes.addFlashAttribute("notificationMessage", "Task created successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }


    /**
     * Handles POST requests to delete a task.
     * Removes the task from the database and clears its associations with teachers.
     *
     * @param id                  The ID of the task to be deleted.
     * @param request             The HttpServletRequest object used to retrieve the referring page.
     * @param redirectAttributes  Used to pass flash attributes for notification messages.
     *
     * @return Redirects to the referring page after successfully deleting the task.
     *
     * @throws IllegalArgumentException if the task ID is invalid.
     */
    @PostMapping("/task/delete/{id}")
    public String deleteTask(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid task Id: " + id));

        for (Teacher teacher : task.getTeachers()) {
            teacher.getTasks().remove(task);
            teacherRepository.save(teacher);
        }
        task.getTeachers().clear();
        taskRepository.delete(task);
        redirectAttributes.addFlashAttribute("notificationMessage", "Task deleted successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        return "redirect:" + request.getHeader("Referer");
    }


    /**
     * Handles POST requests for a teacher to join or unjoin a task.
     * Uses the logged-in teacher's ID from the session to update task associations.
     *
     * @param id                  The ID of the task to join or unjoin.
     * @param session             The HttpSession object to retrieve the logged-in teacher's ID.
     * @param request             The HttpServletRequest object used to retrieve the referring page.
     * @param redirectAttributes  Used to pass flash attributes for notification messages.
     *
     * @return Redirects to the referring page after updating the task association.
     *
     * @throws IllegalArgumentException if the task or teacher ID is invalid.
     */
    @PostMapping("/task/join/{id}")
    public String joinTask(@PathVariable Long id, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Long teacherId = (Long) session.getAttribute("loggedInTeacherId"); // Retrieve teacher ID from session

        if (teacherId == null) {

            return "redirect:/login"; // If no teacher is logged in, redirect to login page
        }

        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid task Id: " + id));
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id: " + teacherId));

        if (task.getTeachers().contains(teacher)) {
            task.getTeachers().remove(teacher);
            teacher.getTasks().remove(task);
            redirectAttributes.addFlashAttribute("notificationMessage", "You have unjoined successfully!");
            redirectAttributes.addFlashAttribute("notificationType", "information");
        }
        else{
            task.getTeachers().add(teacher);
            teacher.getTasks().add(task);
            redirectAttributes.addFlashAttribute("notificationMessage", "You have joined successfully!");
            redirectAttributes.addFlashAttribute("notificationType", "information");
        }

        taskRepository.save(task);
        teacherRepository.save(teacher);



        return "redirect:" + request.getHeader("Referer");
    }


    /**
     * Handles POST requests for a teacher to join a task as the main teacher.
     * Ensures that the task does not already have a main teacher assigned.
     *
     * @param id                  The ID of the task to join as the main teacher.
     * @param session             The HttpSession object to retrieve the logged-in teacher's ID.
     * @param request             The HttpServletRequest object used to retrieve the referring page.
     * @param redirectAttributes  Used to pass flash attributes for notification messages.
     *
     * @return Redirects to the referring page after successfully assigning the main teacher.
     *
     * @throws IllegalArgumentException if the task or teacher ID is invalid.
     * @throws RuntimeException if the task already has a main teacher assigned.
     */
    @PostMapping("/task/join-main/{id}")
    public String joinTaskAsMain(@PathVariable Long id, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Long teacherId = (Long) session.getAttribute("loggedInTeacherId"); // Retrieve teacher ID from session
        if (teacherId == null) {
            return "redirect:/login"; // If no teacher is logged in, redirect to login page
        }

        Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid task Id: " + id));

        if (task.getMainTeacher() != null) {
            throw new RuntimeException("Task already has a main teacher assigned");
        }
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id: " + teacherId));

        task.setMainTeacher(teacher);
        if (!task.getTeachers().contains(teacher)) {
            task.getTeachers().add(teacher);
            teacher.getTasks().add(task);
        }

        redirectAttributes.addFlashAttribute("notificationMessage", "You have joined as main successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "information");

        taskRepository.save(task);
        teacherRepository.save(teacher);

        return "redirect:" + request.getHeader("Referer");
    }


    /**
     * Handles POST requests to edit an existing task.
     * Updates the task's details and optionally associates it with a new activity.
     *
     * @param id         The ID of the task to be edited.
     * @param title      The updated title of the task (required).
     * @param description The updated description of the task (optional).
     * @param taskOrder  The updated display order of the task (optional).
     * @param type       The updated type of the task (required).
     * @param endDate    The updated end date of the task (optional).
     * @param startDate  The updated start date of the task (optional).
     * @param activityId The ID of the associated activity (optional).
     * @param request    The HttpServletRequest object used to retrieve the referring page.
     *
     * @return Redirects to the referring page after successfully updating the task.
     *
     * @throws IllegalArgumentException if the task ID is invalid.
     * @throws RuntimeException if the provided task type is invalid.
     */
    @PostMapping("/task/edit/{id}")
    public String editTask(@PathVariable Long id,
                           @RequestParam(required = true) String title,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) Integer taskOrder,
                           @RequestParam(required = true) String type,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date endDate,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date startDate,
                           @RequestParam(required = false) Long activityId,
                           HttpServletRequest request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task Id: " + id));

        task.setTitle(title);
        task.setDescription(description);
        task.setOrder(taskOrder);
        task.setEndDate(endDate);
        task.setStartDate(startDate);

        try {
            Task.TaskType taskType = Task.TaskType.valueOf(type);
            task.setType(taskType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task type: " + type);
        }

        if (activityId != null) {
            Activity activity = activityRepository.findById(activityId).orElse(null);
            task.setActivity(activity);
        }

        taskRepository.save(task);

        return "redirect:" + request.getHeader("Referer");
    }

}
