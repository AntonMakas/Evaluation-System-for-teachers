package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.Course;
import com.mycompany.vut4.model.Student;
import com.mycompany.vut4.repository.CourseRepository;
import com.mycompany.vut4.repository.StudentRepository;
import com.mycompany.vut4.repository.TeamRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;


    /**
     * Handle PostMaping("/uploadStudent") endpoint
     * Process upload students info in csv file, and create Student instance in database
     * If a student already in database, give an duplicate insert student worning
     * Else import sucessfully
     * @param file
     * @param redirectAttributes
     * @return 
     */
    @PostMapping("/uploadStudent")
    public ModelAndView uploadStudentFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            System.out.println(" --- StudentController - uploadStudentFile");
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            System.out.println("Uploaded file content:\n" + content);

            Map<String, Course> courseMap = courseRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(Course::getName, Function.identity()));

            List<Student> students = new ArrayList<>();
            String[] lines = content.split("\n");
            String[] dataLines = Arrays.copyOfRange(lines, 1, lines.length);
            for (String line : dataLines) {
                String[] fields = line.split(",");
                if (fields.length < 5 || fields[1].trim().isEmpty() || fields[2].trim().isEmpty()) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }

                Student student = new Student();
                List<Course> courses = new ArrayList<>();
                if (fields[4] != null && !fields[4].isEmpty()) {
                    String[] courseNames = fields[4].split(";");
                    for (String courseName : courseNames) {
                        Course course = courseMap.get(courseName.trim());
                        System.out.println(course);
                        if (course == null) {
                            throw new IllegalArgumentException("Course not found: " + courseName);
                        }
                        courses.add(course);
                    }
                }
                student.setEmail(fields[3].trim());
                Student existingstudent=studentRepository.findStudentByEmail(student.getEmail());
                if (existingstudent!=null) {
                    existingstudent.addCourses(courses);
                    studentRepository.save(existingstudent);
                    students.add(existingstudent);
                }else {
                    student.setCourses(courses);
                    student.setFirstName(fields[1].trim());
                    student.setLastName(fields[2].trim());
                    studentRepository.save(student);
                    students.add(student);
                }
            }
            students.forEach(System.out::println);
            Map<String, String> response = new HashMap<>();
            redirectAttributes.addFlashAttribute("notificationMessage", "File uploaded successfully!");
            redirectAttributes.addFlashAttribute("notificationType", "danger");
            return new ModelAndView("redirect:/home");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("notificationMessage", "Error processing file: " + e.getMessage());
            redirectAttributes.addFlashAttribute("notificationType", "danger");
            return new ModelAndView("redirect:/home");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("notificationMessage", "File upload failed!");
            redirectAttributes.addFlashAttribute("notificationType", "danger");
            return new ModelAndView("redirect:/home");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("notificationMessage", "Unexpected error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("notificationType", "danger");
            return new ModelAndView("redirect:/home");
        }
    }



}
