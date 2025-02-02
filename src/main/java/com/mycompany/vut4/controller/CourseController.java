package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.*;
import com.mycompany.vut4.repository.*;
import jakarta.servlet.http.HttpSession;
import com.mycompany.vut4.repository.ActivityRepository;
import com.mycompany.vut4.repository.CourseRepository;
import com.mycompany.vut4.repository.TaskRepository;
import com.mycompany.vut4.repository.TeacherRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private CriteriaRepository theCriteriaRepository;

    @Autowired
    private GroupRepository theGroupRepository;

    @Autowired
    private TeacherRepository teacherRepository;
    
    /**
     * Creates a new course object
     * @param redirectAttributes
     * @param request
     * @param name
     * @return 
     */
    @PostMapping("/course/createCourse")
    public String createCourse(
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            @RequestParam("name") String name) {
        if (courseRepository.existsByName(name)) {
            redirectAttributes.addFlashAttribute("notificationMessage", "The course '" + name + "' already exists.");
            redirectAttributes.addFlashAttribute("notificationType", "danger");
            String referer = request.getHeader("Referer");
            return "redirect:" + referer;
        }

        Course course = new Course();
        course.setName(name);
        courseRepository.saveAndFlush(course);

        redirectAttributes.addFlashAttribute("notificationMessage", "Course '" + name + "' has been successfully created!");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }

    
    /**
     * Registers a teacher to a course
     * @param redirectAttributes
     * @param teacherId
     * @param courseId
     * @param model
     * @return 
     */
    @PostMapping("/course/registerToCourse")
    public String registerCourse(
                                RedirectAttributes redirectAttributes,
                                @RequestParam("teacherId") Long teacherId,
                                @RequestParam("courseId") Long courseId,
                                Model model) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher not found: " + teacherId);
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        if (!teacher.getCourses().contains(course)) {
            teacher.getCourses().add(course);
            teacherRepository.saveAndFlush(teacher);

            course.getTeachers().add(teacher);
            courseRepository.saveAndFlush(course);

            redirectAttributes.addFlashAttribute("notificationMessage", "Registration successful!");
            redirectAttributes.addFlashAttribute("notificationType", "success");
        }

        return "redirect:/home";
    }


    @PostMapping("/courses/update/{id}")
    public String updateCourse(@PathVariable Long id, @RequestParam String name) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
        course.setName(name);
        courseRepository.save(course);
        return "redirect:/courses";
    }


    /**
     * Handles GET requests to the "/course/{courseId}" endpoint.
     * Retrieves detailed information about a specific course, including its activities, tasks,
     * and other related data. Adds this information to the model for rendering on the course details page.
     *
     * @param courseId The ID of the course whose details are being retrieved.
     * @param model    The Model object used to pass attributes to the view.
     * @param session  The HttpSession object used to retrieve the logged-in teacher's ID.
     * @param request  The HttpServletRequest object used to retrieve the current URL.
     *
     * @return         The name of the view to be rendered, which is "course".
     *
     * @throws IllegalArgumentException if the course ID is invalid and the course cannot be found.
     */
    @GetMapping("/course/{courseId}")
    public String getCourseDetails(@PathVariable Long courseId, Model model, HttpSession session,HttpServletRequest request) {
        String currentUrl = request.getRequestURI();
        model.addAttribute("currentUrl", currentUrl);
        Long teacherId = (Long) session.getAttribute("loggedInTeacherId");
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id: " + courseId));

        List<Activity> activities = Optional.ofNullable(activityRepository.findByCourseId(courseId)).orElse(Collections.emptyList());
        List<Task> tasks = Optional.ofNullable(taskRepository.findByCourseId(courseId)).orElse(Collections.emptyList());
        model.addAttribute("course", course);
        model.addAttribute("activities", activities);
        model.addAttribute("activityTypes", Arrays.asList(Activity.ActivityType.values()));
        model.addAttribute("taskTypes", Arrays.asList(Task.TaskType.values()));
        model.addAttribute("tasks", tasks);

        model.addAttribute("teacherId", teacherId);
        model.addAttribute("criteriaTypes", Arrays.asList(Criteria.CriteriaType.values()));

        Map<Long, List<Group>> activityGroupsMap = activities.stream()
                .collect(Collectors.toMap(
                        Activity::getId,
                        activity -> theGroupRepository.findByActivityId(activity.getId())
                ));
        model.addAttribute("activityGroupsMap", activityGroupsMap);


        return "course"; 
    }

}
