package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.Teacher;
import com.mycompany.vut4.model.Course;
import com.mycompany.vut4.model.Task;
import com.mycompany.vut4.repository.TeacherRepository;
import com.mycompany.vut4.repository.CourseRepository;
import com.mycompany.vut4.repository.TaskRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.mycompany.vut4.model.Task.TaskStatus.doing;

@Controller
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TaskRepository taskRepository;
    
    private class MyResponse<T> {
        private String status;
        private T data;
        
         public MyResponse(String status, T data) {
            this.status = status;
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }



    /**
     * Handel GET requests to the “/home” endpoint
     * Create a view of home page
     * @param model
     * @param request
     * @param session
     * @return 
     */
    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request, HttpSession session) {
        Long teacherId = (Long) session.getAttribute("loggedInTeacherId"); 
        String currentUrl = request.getRequestURI();
        model.addAttribute("currentUrl", currentUrl);
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("teacherId", teacherId);
        return "home";
    }

    /**
     * Handel GET requests to the “/home/fetchCourses” endpoint
     * Fetch courses for which an instructor is registered with an instructor ID
     * If courses are found, return a list of necessary for each course
     * @param teacherId
     * @return 
     */
    @GetMapping("/home/fetchCourses")
    @ResponseBody
    public ResponseEntity<?> searchCourseByTeacherId(@RequestParam Long teacherId,Model model) {
        try {
            List<Course> courses = courseRepository.findCoursesByTeacherId(teacherId);
            List<CourseByTeacherDTO> result = courses.stream()
                    .map(course -> new CourseByTeacherDTO(course.getId(), course.getName()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    /**
     * Handle GetMapping("/home/fetchUnregisteredCourse") endpoint
     * Select unregistered course for logged teacher
     * Return necessary info of courses
     * @param teacherId
     * @return
     */
    @GetMapping("/home/fetchUnregisteredCourse")
    @ResponseBody
    public ResponseEntity<?> searchUnregisteredCourseByTeacherId(@RequestParam Long teacherId) {
        try {
            if (teacherId == null || teacherId <= 0) {
                return ResponseEntity.badRequest().body("Invalid teacher ID provided.");
            }
            List<Course> courses = courseRepository.findCoursesByTeacherId(teacherId);
            List<Course> allCourses = courseRepository.findAll();

            Set<Long> registeredCourseIds = courses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet());

            List<Course> unregisteredCourses = allCourses.stream()
                    .filter(course -> !registeredCourseIds.contains(course.getId()))
                    .collect(Collectors.toList());

            List<CourseByTeacherDTO> unregisteredResult = unregisteredCourses.stream()
                    .map(course -> new CourseByTeacherDTO(course.getId(), course.getName()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(unregisteredResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }

    /**
     * Handel GET requests to the “/home/fetchTasks” endpoint
     * Fetch fetchTasks for which a teacher is registered with a teacherId
     * If tasks are found, return a list of necessary for each course
     * @param teacherId
     * @return 
     */
    @GetMapping("/home/fetchTasks")
    @ResponseBody
    public List<TaskByTeacherDTO> searchDoingTaskByTeacherId(@RequestParam Long teacherId) {
        List<Task> tasks = taskRepository.searchDoingTaskByTeacherId(teacherId,doing);
        return tasks.stream().sorted(Comparator.comparing(Task::getOrder))
                .map(task -> new TaskByTeacherDTO(task.getId(), task.getTitle(),task.getOrder(),task.getEndDate(),task.getCourse().getId()))
                .collect(Collectors.toList());
    }

    /**
     * Handles GET requests to the "/login/getTeacher" endpoint
     * Verifies if the specified login references a teacher in the database
     * If it exists, adds the teacherId to the session
     * @param login
     * @param session
     * @return 
     */
    @GetMapping(value = "/login/getTeacher", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MyResponse<Object>> getTeacherByLogin(@RequestParam String login, HttpSession session, Model model) {
        Teacher teacher = teacherRepository.findByLogin(login);
        System.out.println("Checking teacher for login: " + login); // Log the login to check incoming request
        if (teacher != null) {
            Long teacherId = teacher.getId();
            session.setAttribute("loggedInTeacherId", teacherId);
            MyResponse<Object> response = new MyResponse<>("success", "");
            System.out.println("Checking teacher for id: " + teacher.getId()); // Log the login to check incoming request
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            MyResponse<Object> response = new MyResponse<>("error", "Invalid login");
            System.out.println("Teacher not found for login: " + login); 
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Handle GetMapping("/getLoggedTeacher") endpoint
     * Try to get the logged teacher from session
     * @param session
     * @return 
     */
    @GetMapping(value = "/getLoggedTeacher", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MyResponse<Object>> getLoggedTeacher(HttpSession session) {
        Long teacherId = (Long) session.getAttribute("loggedInTeacherId");
        if (teacherId == null) {
            return new ResponseEntity<>(new MyResponse<>("error", "Not logged in"), HttpStatus.UNAUTHORIZED);
        }

        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        if (teacher == null) {
            return new ResponseEntity<>(new MyResponse<>("error", "Teacher not found"), HttpStatus.NOT_FOUND);
        }

        TeacherDTO teacherDTO = new TeacherDTO(teacher.getId(), teacher.getLogin());

        return new ResponseEntity<>(new MyResponse<>("success", teacherDTO), HttpStatus.OK);
    }

    /**
     * Handel log out event for logged in teacher
     * Destory session and redirect to login page
     * @param session
     * @return 
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}

class CourseByTeacherDTO {
    private Long id;
    private String name;

    public CourseByTeacherDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
class TaskByTeacherDTO {
    private Long id;
    private Integer order;
    private String title;
    private Date endDate;
    private Long course_id;
    public TaskByTeacherDTO(Long id, String title,Integer order, Date endDate,Long course_id) {
        this.id = id;
        this.order = order;
        this.title = title;
        this.endDate = endDate;
        this.course_id = course_id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getCourse_id() {
        return course_id;
    }

    public void setCourse_id(Long course_id) {
        this.course_id = course_id;
    }

}

 class TeacherDTO {
    private Long id;
    private String login;

    // Constructor
    public TeacherDTO(Long id, String login) {
        this.id = id;
        this.login = login;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}