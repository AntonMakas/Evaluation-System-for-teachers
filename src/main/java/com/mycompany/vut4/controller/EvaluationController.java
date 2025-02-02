package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.*;
import com.mycompany.vut4.service.EvaluationService;
import com.mycompany.vut4.service.EvaluationService.CriteriaDTO;
import com.mycompany.vut4.service.EvaluationService.EvaluationResult;
import com.mycompany.vut4.service.EvaluationService.EvaluationUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
    * Handles GET requests to the "/evaluation" endpoint.
    * Retrieves the groups, teams and students associated to an activity,
    * as well as other attributes useful for the initialization of the page
    * 
    * @param model      The object used to pass attributes to the view.
    * @param session
    * @param request
    * @param activityId The ID of the activity for which data is being retrieved.
    *                   This value is required as a request parameter.
    * 
    * @return The name of the view to be rendered, which is "evaluation".
    */
    @GetMapping("/evaluation")
    public String getCourses(Model model, HttpSession session,HttpServletRequest request, @RequestParam Long activityId) {
        Long teacherId = (Long) session.getAttribute("loggedInTeacherId"); 
        String currentUrl = request.getRequestURI();
        Activity activity = evaluationService.getActivity(activityId);
        List<Group> groups = evaluationService.getGroups(activityId);
        List<Team> teams = evaluationService.getTeams(activityId);
        List<Task> tasks = evaluationService.getTasks(activityId);
        List<Student> students = evaluationService.getStudents(teams);

        model.addAttribute("groups", groups);
        model.addAttribute("activityId", activityId);
        model.addAttribute("activityType", activity.getType());
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("teams", teams);
        model.addAttribute("students", students);
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentUrl", currentUrl);
        model.addAttribute("courseId", activity.getCourse().getId());

        return "evaluation";
    }

    /**
    * Handles GET requests to the "/evaluation/criteria" endpoint.
    * Retrieves the groups, teams and students associated to an activity.
    * Pass to the view : groups, activityId, activityType, teams, students
    * 
    * @param groupId    The ID of the group for which data is being retrieved.
    *                   This value is required as a request parameter.
    * 
    * @return a list of criterias, for each criteria : id, name, type
    */
    @GetMapping("/evaluation/criteria")
    @ResponseBody
    public List<CriteriaDTO> getCriteria(@RequestParam Long groupId) {
        return evaluationService.getCriteria(groupId);
    }

    /**
    * Handles GET requests to the "/evaluation/getCriterionTableTeam" endpoint.
    * For the specified criterion evaluated for a team, gets the team evaluation 
    * results for each team registered to the activity. 
    * Adds an attribute to the view "evaluationData" containing the retrieved informations.
    * 
    * @param model      The object used to pass attributes to the view.
    * @param activityId The ID of the activity for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @param criterionId The ID of the criterion for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @return the fragment to render the table for team criterion evaluation
    */
    @GetMapping("/evaluation/getCriterionTableTeam")
    public String getCriterionTableTeam(Model model, @RequestParam Long criterionId, @RequestParam Long activityId) {
        List<Map<String, Object>> evaluationData = evaluationService.getCriterionTableTeam(criterionId, activityId);
        model.addAttribute("evaluationData", evaluationData);
        return "fragments/evaluationTableCriteria :: evaluationTableCriteria";
    }

    /**
    * Handles GET requests to the "/evaluation/getCriterionTableIndividual" endpoint.
    * For the specified criterion evaluated individually, gets the individual evaluation 
    * results for each student registered to the activity. 
    * Adds an attribute to the view "evaluationData" containing the retrieved informations.
    * 
    * @param model      The object used to pass attributes to the view.
    * @param activityId The ID of the activity for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @param criterionId The ID of the criterion for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @return the fragment to render the table for individual criterion evaluation
    */
    @GetMapping("/evaluation/getCriterionTableIndividual")
    public String getCriterionTableIndividual(Model model, @RequestParam Long criterionId, @RequestParam Long activityId) {
        List<Map<String, Object>> evaluationData = evaluationService.getCriterionTableIndividual(criterionId, activityId);
        model.addAttribute("evaluationData", evaluationData);
        return "fragments/evaluationTableCriteria :: evaluationTableCriteria";
    }

    /**
    * Handles GET requests to the "/evaluation/getTeamTable" endpoint.
    * For the specified team, gets the team evaluation results for each team criteria
    * in the activity. 
    * Adds an attribute to the view "evaluationData" containing the retrieved informations.
    * 
    * @param model      The object used to pass attributes to the view.
    * @param activityId The ID of the activity for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @param teamId     The ID of the team for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @return the fragment to render the table for the results of a team
    */
    @GetMapping("/evaluation/getTeamTable")
    public String getTeamEvaluation(Model model, @RequestParam Long teamId, @RequestParam Long activityId) {
        List<Map<String, Object>> evaluationData = evaluationService.getTeamEvaluationData(teamId, activityId);
        model.addAttribute("evaluationData", evaluationData);
        return "fragments/evaluationTableTeam :: evaluationTableTeam";
    }
    
    /**
    * Handles GET requests to the "/evaluation/getStudentTable" endpoint.
    * For the specified student, gets the individual evaluation results for each individual
    * criteria in the activity. 
    * Adds an attribute to the view "evaluationData" containing the retrieved informations.
    * 
    * @param model      The object used to pass attributes to the view.
    * @param activityId The ID of the activity for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @param studentId  The ID of the student for which data is being retrieved.
    *                   This value is required as a request parameter.
    * @return the fragment to render the table for the results of a student
    */
    @GetMapping("/evaluation/getStudentTable")
    public String getStudentEvaluation(Model model, @RequestParam Long studentId, @RequestParam Long activityId) {
        List<Map<String, Object>> evaluationData = evaluationService.getStudentEvaluationData(studentId, activityId);
        model.addAttribute("evaluationData", evaluationData);
        return "fragments/evaluationTableTeam :: evaluationTableTeam";
    }

    /**
    * Verify before sending assessment to the students, that all the criteria has been assessed. <br/>
    * For a group : verifies that for each criteria in the specified group, all team and individual evaluations
    * have been assessed for all the teams enrolled in the activity. <br/>
    * For a team : verifies that for each criteria in the specified group, all team and individual evaluations
    * have been assessed for the specified team. <br/>
    * For a student : verifies that for each criteria in the specified group, all team and individual evaluations
    * have bean assessed for the specified student.
    * @param type       The type of the evaluation to verify : either group, team or student
    * @param id         The id of the group, team or student
    * @param activityId The activityId
    * @param groupId    The groupId
    * @return 
    */
    @GetMapping("/evaluation/verify")
    public ResponseEntity<Map<String, Object>> verifyEvaluations(
            @RequestParam String type,
            @RequestParam Long id,
            @RequestParam Long activityId,
            @RequestParam Long groupId) {

        try {
            EvaluationResult evaluationResult = evaluationService.verifyEvaluations(type, id, activityId, groupId);
            Map<String, Object> response = new HashMap<>();
            response.put("missing", evaluationResult.getMissingEvaluations());
            response.put("completedIndEval", evaluationResult.getCompletedIndEval());
            response.put("completedTeamEval", evaluationResult.getCompletedTeamEval());
            response.put("totalTeamPoints", evaluationResult.getTotalTeamPoints());
            response.put("indPoints", evaluationResult.getIndividualPointsMap());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }    
    
    /**
     * Updates the changed evaluation in the database
     * @param updateRequest object containing all the needed information for the update
     * @return 
     */
    @PostMapping("/evaluation/update")
    @ResponseBody
    public Map<String, Object> updateEvaluation(@RequestBody EvaluationUpdateRequest updateRequest) {
        Boolean newData = evaluationService.updateEvaluation(updateRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("newData", newData);
        return response;
    }
    
    /**
     * Returns the preview page where the information about the mail is displayed
     * @return the mail view
     */
    @GetMapping("evaluation/mailPreview")
    public String getStatisticsPreview() {
        return "previewMail";  
    }
}
