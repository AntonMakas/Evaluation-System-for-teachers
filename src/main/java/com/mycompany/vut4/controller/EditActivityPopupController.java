package com.mycompany.vut4.controller;

import com.mycompany.vut4.model.Activity;
import com.mycompany.vut4.model.Criteria;
import com.mycompany.vut4.model.Criteria.CriteriaType;
import com.mycompany.vut4.model.CriteriaEvaluation;
import com.mycompany.vut4.model.Group;
import com.mycompany.vut4.repository.ActivityRepository;
import com.mycompany.vut4.repository.CriteriaEvaluationRepository;
import com.mycompany.vut4.repository.CriteriaRepository;
import com.mycompany.vut4.repository.GroupRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/popupEditActivity")
public class EditActivityPopupController {

    @Autowired
    private CriteriaRepository theCriteriaRepository;

    @Autowired
    private GroupRepository theGroupRepository;

    @Autowired
    private CriteriaEvaluationRepository criteriaEvaluationRepository;

    @Autowired
    private ActivityRepository activityRepository;


    /**
     * Handles POST requests to add a new group to the specified activity.
     *
     * @param groupName           The name of the group to be created.
     * @param request             The HttpServletRequest object used to retrieve the referring page.
     * @param redirectAttributes  Used to pass flash attributes for notification messages.
     * @param activityId          The ID of the activity to which the group will be associated.
     *
     * @return Redirects to the referring page after successfully creating the group.
     *
     * @throws IllegalArgumentException if the provided activity ID is invalid.
     */
    @PostMapping("/add-new-group")
    public String addNewGroup(@RequestParam String groupName,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes,
                              @RequestParam Long activityId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activity ID: " + activityId));

        Group newGroup = new Group();
        newGroup.setName(groupName);
        newGroup.setActivity(activity);

        theGroupRepository.save(newGroup);
        redirectAttributes.addFlashAttribute("notificationMessage", "Group created successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "success");
        return "redirect:" + request.getHeader("Referer");
    }


    /**
     * Handles POST requests to create new criteria for a specified group.
     *
     * @param groupId             The ID of the group for which the criteria will be created.
     * @param request             The HttpServletRequest object used to retrieve the referring page.
     * @param redirectAttributes  Used to pass flash attributes for notification messages.
     * @param criteriaName        The name of the criteria to be created.
     * @param type                The type of the criteria (TEAM or INDIVIDUAL).
     * @param teamSize            A list of team sizes (required for TEAM criteria, optional otherwise).
     * @param maxPoints           A list of maximum points (required for TEAM or INDIVIDUAL criteria).
     *
     * @return Redirects to the referring page after successfully creating the criteria.
     *
     * @throws IllegalArgumentException if the provided group ID is invalid.
     */
    @PostMapping("/criteria-evaluation")
    public String createCriteriaforGroup(@RequestParam Long groupId,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes,
                                         @RequestParam String criteriaName,
                                         @RequestParam CriteriaType type,
                                         @RequestParam(required = false, name = "teamSize[]") List<Integer> teamSize,
                                         @RequestParam(required = false, name = "maxPoints[]") List<Integer> maxPoints) {

        Group group = theGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid group ID: " + groupId));

        Criteria criteria = new Criteria();
        criteria.setName(criteriaName);
        criteria.setType(type);
        criteria.setGroup(group);
        theCriteriaRepository.save(criteria);

        // If it's a TEAM criteria, process teamSize and maxPoints
        if (type == CriteriaType.TEAM) {
            if (teamSize == null || maxPoints == null) {
                redirectAttributes.addFlashAttribute("notificationMessage", "Team size and max points are required for TEAM criteria.");
                redirectAttributes.addFlashAttribute("notificationType", "error");
                return "redirect:" + request.getHeader("Referer");
            }

            List<CriteriaEvaluation> evaluations = new ArrayList<>();
            for (int i = 0; i < teamSize.size(); i++) {
                CriteriaEvaluation evaluation = new CriteriaEvaluation();
                evaluation.setCriteria(criteria);
                evaluation.setTeamSize(teamSize.get(i));
                evaluation.setPoints(maxPoints.get(i));
                evaluations.add(evaluation);
            }

            criteriaEvaluationRepository.saveAll(evaluations);
        }

        if (type == CriteriaType.INDIVIDUAL) {
            if (maxPoints == null || maxPoints.isEmpty()) {
                redirectAttributes.addFlashAttribute("notificationMessage", "Max points are required for INDIVIDUAL criteria.");
                redirectAttributes.addFlashAttribute("notificationType", "error");
                return "redirect:" + request.getHeader("Referer");
            }

            CriteriaEvaluation evaluation = new CriteriaEvaluation();
            evaluation.setCriteria(criteria);
            evaluation.setTeamSize(1);
            evaluation.setPoints(maxPoints.get(0));
            criteriaEvaluationRepository.save(evaluation);
        }

        redirectAttributes.addFlashAttribute("notificationMessage", "Criteria created successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        return "redirect:" + request.getHeader("Referer");
    }


    /**
     * Handles POST requests to remove a group and its associated criteria and criteria evaluations.
     *
     * @param groupId             The ID of the group to be removed.
     * @param redirectAttributes  Used to pass flash attributes for notification messages.
     * @param request             The HttpServletRequest object used to retrieve the referring page.
     *
     * @return Redirects to the referring page after successfully removing the group and its associations.
     */
    @PostMapping("/remove-group")
    @Transactional
    public String removeGroup(@RequestParam Long groupId, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        List<Criteria> criteriaList = theCriteriaRepository.findByGroupId(groupId);
        for (Criteria criteria : criteriaList) {
            criteriaEvaluationRepository.deleteAllByCriteriaId(criteria.getId());
        }

        theCriteriaRepository.deleteByGroupId(groupId);
        theGroupRepository.deleteById(groupId);

        redirectAttributes.addFlashAttribute("notificationMessage", "Group delete successfully!");
        redirectAttributes.addFlashAttribute("notificationType", "success");

        return "redirect:" + request.getHeader("Referer");
    }


}
