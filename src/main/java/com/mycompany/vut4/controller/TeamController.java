package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.Activity;
import com.mycompany.vut4.model.Team;
import com.mycompany.vut4.model.Student;
import com.mycompany.vut4.repository.ActivityRepository;
import com.mycompany.vut4.repository.TeamRepository;
import com.mycompany.vut4.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * Handle PostMaping("/uploadTeam") endpoint
     * Process upload teams info in csv file
     * If students in each already in database, import sucessfully, and create Student instance in database
     * Else if a team is already in database, return an duplicate insert worning
     * @param file
     * @param redirectAttributes
     * @return 
     */
    @PostMapping("/uploadTeam")
    public ModelAndView uploadTeamFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Team uploading...");
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            System.out.println("Uploaded file content:\n" + content);

            Map<String, Student> studentMap = studentRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(Student::getEmail, Function.identity()));

            List<Team> teams = new ArrayList<>();
            String[] lines = content.split("\n");
            String[] dataLines = Arrays.copyOfRange(lines, 1, lines.length);

            for (String line : dataLines) {
                if (line == null || line.trim().isEmpty()) {
                    System.out.println("Skipping empty line");
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length < 6 || fields[1].trim().isEmpty() || fields[2].trim().isEmpty()) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }
                Long activityId;
                try {
                    activityId = Long.parseLong(fields[6].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid activity ID: " + fields[6]);
                }
                if (teamRepository.findTeamInDatabase(fields[1].trim(), activityId) != null) {
                    System.out.println("Skipping team already exists:  " + line);
                    continue;
                }

                Team team = new Team();
                team.setName(fields[1].trim());
                team.setLeaderIdx(Integer.parseInt(fields[2].trim()));
                team.setNbStudents(Integer.parseInt(fields[3].trim()));
                team.setDescription(fields[5].trim());

                Activity activity = activityRepository.findById(activityId)
                        .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));
                team.setActivity(activity);

                if (fields[4] != null && !fields[4].isEmpty()) {
                    String[] studentEmails = fields[4].split(";");
                    List<Student> students = new ArrayList<>();
                    for (String studentEmail : studentEmails) {
                        Student student = studentMap.get(studentEmail.trim());
                        if (student == null) {
                            throw new IllegalArgumentException("Student not found: " + studentEmail.trim());
                        }
                        students.add(student);
                    }
                    team.setStudents(students);

                }

                if (team.getActivity() == null || team.getActivity().getId() == null) {
                    throw new IllegalArgumentException("Team activity is not set for team: " + team.getName());
                }

                teamRepository.save(team);
                for(Student student : team.getStudents()) {
                    student.addTeams(team);
                }
                teams.add(team);
            }

            teams.forEach(System.out::println);

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
