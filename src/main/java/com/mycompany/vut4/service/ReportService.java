package com.mycompany.vut4.service;

import com.mycompany.vut4.model.Activity;
import com.mycompany.vut4.repository.ActivityRepository;
import com.mycompany.vut4.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import com.mycompany.vut4.model.Report;
import com.mycompany.vut4.model.Teacher;
import com.mycompany.vut4.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ActivityRepository activityRepository;

    /**
     * Creates a Report object with the given information
     * @param hours         How many hours spent
     * @param proportion    Proportion of students evaluated
     * @param nbStudents    Number of students evaluated
     * @param description   Description for the report
     * @param teacherId     Teacher who did the report
     * @param activityId    Activity for which the report was done
     */
    public void addReport(Integer hours, String proportion, Integer nbStudents, String description, Long teacherId, Long activityId) {
        Report report = new Report();

        if (hours != -1) {
            report.setHours(hours);
        }
        if (!proportion.isEmpty()) {
            report.setProportion(proportion);
        }
        if (nbStudents != -1) {
            report.setNbStudents(nbStudents);
        }

        report.setDescription(description);

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id: " + teacherId));
        report.setTeacher(teacher);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activity Id: " + activityId));
        report.setActivity(activity);

        reportRepository.saveAndFlush(report);
    }
    
    /**
     * Retrieves information of all reports for the specified activity
     * @param activityId
     * @return popup thymeleaf fragment
     */
    public List<Map<String, Object>> getActivityStatistics(Long activityId) {
        List<Report> reports = reportRepository.findByActivityId(activityId);

        Map<Teacher, List<Report>> reportsByTeacher = reports.stream()
                .collect(Collectors.groupingBy(Report::getTeacher));

        List<Map<String, Object>> statistics = new ArrayList<>();
        for (Map.Entry<Teacher, List<Report>> entry : reportsByTeacher.entrySet()) {
            Teacher teacher = entry.getKey();
            List<Report> teacherReports = entry.getValue();

            List<Map<String, String>> simplifiedReports = teacherReports.stream()
                    .map(report -> Map.of(
                            "hours", report.getHours() != null ? report.getHours().toString() : "N/A",
                            "nbStudents", report.getNbStudents() != null ? report.getNbStudents().toString() : "N/A",
                            "proportion", report.getProportion() != null ? report.getProportion() : "N/A",
                            "description", report.getDescription() != null ? report.getDescription() : "No description provided"
                    ))
                    .collect(Collectors.toList());

            int totalHours = teacherReports.stream()
                    .mapToInt(report -> report.getHours() != null ? report.getHours() : 0)
                    .sum();

            int totalStudents = teacherReports.stream()
                    .mapToInt(report -> report.getNbStudents() != null ? report.getNbStudents() : 0)
                    .sum();

            Map<String, Object> teacherStats = new HashMap<>();
            teacherStats.put("teacherLogin", teacher.getLogin());
            teacherStats.put("reports", simplifiedReports);
            teacherStats.put("totalHours", totalHours);
            teacherStats.put("totalStudents", totalStudents);

            statistics.add(teacherStats);
        }

        return statistics;
    }
}