package com.mycompany.vut4.controller;

import com.mycompany.vut4.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Handles POST requests to the "/report/add" endpoint.
     * Creates a Report object with the given information
     * @param redirectAttributes    
     * @param request
     * @param hours         How many hours spent
     * @param proportion    Proportion of students evaluated
     * @param nbStudents    Number of students evaluated
     * @param description   Description for the report
     * @param teacherId     Teacher who did the report
     * @param activityId    Activity for which the report was done
     * @return redirect on the page where the user was
     */
    @PostMapping("/report/add")
    public String addReport(RedirectAttributes redirectAttributes,
                            HttpServletRequest request,
                            @RequestParam(required = false, defaultValue = "-1") Integer hours,
                            @RequestParam(required = false, defaultValue = "") String proportion,
                            @RequestParam(required = false, defaultValue = "-1") Integer nbStudents,
                            @RequestParam(required = true) String description,
                            @RequestParam(required = true) Long teacherId,
                            @RequestParam(required = true) Long activityId) {

        reportService.addReport(hours, proportion, nbStudents, description, teacherId, activityId);
        redirectAttributes.addFlashAttribute("notificationMessage", "Report successfully saved.");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }
    
    /**
     * Handles POST requests to the "/course/statistics" endpoint.
     * Retrieves information of all report for the specified activity
     * @param activityId
     * @param model
     * @return popup thymeleaf fragment
     */
    @GetMapping("/course/statistics")
    public String getActivityStatistics(@RequestParam(required = true) Long activityId, Model model) {
        List<Map<String, Object>> statistics = reportService.getActivityStatistics(activityId);
        model.addAttribute("statistics", statistics);
        return "fragments/popupStatistics :: statisticsModal";
    }

}
