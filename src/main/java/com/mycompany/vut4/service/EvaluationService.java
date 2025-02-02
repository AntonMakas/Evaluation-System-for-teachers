package com.mycompany.vut4.service;

import com.mycompany.vut4.model.*;
import com.mycompany.vut4.model.Criteria.CriteriaType;
import com.mycompany.vut4.repository.*;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService {

    @Autowired
    private CriteriaRepository criteriaRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private IndividualEvaluationRepository individualEvaluationRepository;
    @Autowired
    private TeamEvaluationRepository teamEvaluationRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TaskRepository taskRepository;

    public Activity getActivity(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found for id: " + activityId));
    }

    public List<Group> getGroups(Long activityId) {
        return groupRepository.findByActivityId(activityId);
    }

    public List<Team> getTeams(Long activityId) {
        return teamRepository.findByActivityId(activityId);
    }

    public List<Task> getTasks(Long activityId) {
        return taskRepository.findByActivityId(activityId);
    }

    public List<Student> getStudents(List<Team> teams) {
        List<Student> students = new ArrayList<>();
        for (Team team : teams) {
            students.addAll(team.getStudents());
        }
        return students;
    }

    public List<CriteriaDTO> getCriteria(Long groupId) {
        List<Criteria> criteria = criteriaRepository.findByGroupId(groupId);
        return criteria.stream()
                .map(c -> new CriteriaDTO(c.getId(), c.getName(), c.getType()))
                .collect(Collectors.toList());
    }

    /**
     * For the specified criterion evaluated for a team, gets the team evaluation
     * results for each team registered to the activity.
     *
     * @param activityId The ID of the activity for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @param criterionId The ID of the criterion for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @return evaluationData containing the information to display in the table
     */
    public List<Map<String, Object>> getCriterionTableTeam(Long criterionId, Long activityId) {
        Criteria criteria = criteriaRepository.findById(criterionId)
                .orElseThrow(() -> new IllegalArgumentException("Criterion not found for id: " + criterionId));

        List<Team> teams = teamRepository.findByActivityId(activityId);
        List<CriteriaEvaluation> criteriaEvaluations = criteria.getCriteriaEvaluations();

        Map<Integer, CriteriaEvaluation> criteriaEvalBySize = criteriaEvaluations.stream()
                .collect(Collectors.toMap(CriteriaEvaluation::getTeamSize, ce -> ce));

        List<Map<String, Object>> evaluationData = new ArrayList<>();

        for (Team team : teams) {
            int teamSize = team.getStudents().size();
            CriteriaEvaluation criteriaEvaluation = criteriaEvalBySize.get(teamSize);
            if (criteriaEvaluation == null) {
                throw new IllegalArgumentException("No CriteriaEvaluation found for team size: " + teamSize);
            }

            TeamEvaluation teamEvaluation = teamEvaluationRepository.findByTeamIdAndCriteriaId(team.getId(), criterionId);

            Map<String, Object> teamData = new HashMap<>();
            teamData.put("teamId", team.getId());
            teamData.put("criterionId", criterionId);
            teamData.put("studentId", null);
            teamData.put("evaluationId", teamEvaluation != null ? teamEvaluation.getId() : null);
            teamData.put("type", criteria.getType());
            teamData.put("name", team.getName());
            teamData.put("pointsObtained", teamEvaluation != null ? teamEvaluation.getPoints() : null);
            teamData.put("feedback", teamEvaluation != null ? teamEvaluation.getFeedback() : null);
            teamData.put("maxPoints", criteriaEvaluation.getPoints());

            evaluationData.add(teamData);
        }

        return evaluationData;
    }

    /**
     * For the specified criterion evaluated individually, gets the individual evaluation
     * results for each student registered to the activity.
     *
     * @param activityId The ID of the activity for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @param criterionId The ID of the criterion for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @return evaluationData containing the information to display in the table
     */
    public List<Map<String, Object>> getCriterionTableIndividual(Long criterionId, Long activityId) {
        Criteria criteria = criteriaRepository.findById(criterionId)
                .orElseThrow(() -> new IllegalArgumentException("Criterion not found for id: " + criterionId));

        List<Team> teams = teamRepository.findByActivityId(activityId);
        CriteriaEvaluation criteriaEvaluation = criteria.getCriteriaEvaluations().get(0);

        List<Map<String, Object>> evaluationData = new ArrayList<>();

        for (Team team : teams) {
            for (Student student : team.getStudents()) {
                IndividualEvaluation individualEvaluation = individualEvaluationRepository.findByStudentIdAndCriteriaId(student.getId(), criterionId);

                Map<String, Object> data = new HashMap<>();
                data.put("teamId", team.getId());
                data.put("criterionId", criterionId);
                data.put("studentId", student.getId());
                data.put("evaluationId", individualEvaluation != null ? individualEvaluation.getId() : null);
                data.put("type", criteria.getType());
                data.put("name", student.getFirstName() + ' ' + student.getLastName());
                data.put("pointsObtained", individualEvaluation != null ? individualEvaluation.getPoints() : null);
                data.put("feedback", individualEvaluation != null ? individualEvaluation.getFeedback() : null);
                data.put("maxPoints", criteriaEvaluation.getPoints());

                evaluationData.add(data);
            }
        }

        return evaluationData;
    }

    /**
     * Updates the changed evaluation in the database
     * @param updateRequest object containing all the needed information for the update
     * @return true if a new team/individual evaluation object was created
     */
    @Transactional
    public boolean updateEvaluation(EvaluationUpdateRequest updateRequest) {
        Boolean newData = false;
        Criteria criteria = criteriaRepository.findById(updateRequest.criteriaId)
                .orElseThrow(() -> new IllegalArgumentException("Criterion not found for id: " + updateRequest.criteriaId));

        if (updateRequest.getType() == CriteriaType.TEAM) {
            TeamEvaluation teamEvaluation = (updateRequest.getEvaluationId() != null)
                    ? teamEvaluationRepository.findById(updateRequest.getEvaluationId()).orElse(null)
                    : null;
            Team team = teamRepository.findById(updateRequest.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Team not found for id: " + updateRequest.teamId));

            if (teamEvaluation == null) {
                newData = true;
                teamEvaluation = new TeamEvaluation();
                teamEvaluation.setCriteria(criteria);
                teamEvaluation.setTeam(team);
            }

            switch (updateRequest.getField()) {
                case "points":
                    teamEvaluation.setPoints(Integer.valueOf(updateRequest.getValue()));
                    break;
                case "feedback":
                    teamEvaluation.setFeedback(updateRequest.getValue());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + updateRequest.getField());
            }

            teamEvaluationRepository.saveAndFlush(teamEvaluation);

        } else {
            IndividualEvaluation individualEvaluation = individualEvaluationRepository.findByStudentIdAndCriteriaId(
                    updateRequest.getStudentId(), updateRequest.getCriteriaId());

            Student student = studentRepository.findById(updateRequest.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found for id: " + updateRequest.studentId));

            if (individualEvaluation == null) {
                newData = true;
                individualEvaluation = new IndividualEvaluation();
                individualEvaluation.setStudent(student);
                individualEvaluation.setCriteria(criteria);
            }

            switch (updateRequest.getField()) {
                case "points":
                    individualEvaluation.setPoints(Integer.valueOf(updateRequest.getValue()));
                    break;
                case "feedback":
                    individualEvaluation.setFeedback(updateRequest.getValue());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + updateRequest.getField());
            }

            individualEvaluationRepository.saveAndFlush(individualEvaluation);
        }

        return newData;
    }

    /**
     * For the specified team, gets the team evaluation results for each team criteria
     * in the activity.
     *
     * @param activityId The ID of the activity for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @param teamId     The ID of the team for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @return evaluationData containing the information to display in the table
     */
    public List<Map<String, Object>> getTeamEvaluationData(Long teamId, Long activityId) {
        List<Group> groups = groupRepository.findByActivityId(activityId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found for id: " + teamId));
        List<Map<String, Object>> evaluationData = new ArrayList<>();

        for (Group group : groups) {
            List<Criteria> criteriaList = criteriaRepository.findByGroupId(group.getId());

            Map<String, Object> groupData = new HashMap<>();
            groupData.put("groupName", group.getName());

            List<Map<String, Object>> criteriaData = new ArrayList<>();
            for (Criteria criteria : criteriaList) {
                if (criteria.getType() == CriteriaType.TEAM) {
                    TeamEvaluation evaluation = teamEvaluationRepository.findByTeamIdAndCriteriaId(teamId, criteria.getId());
                    List<CriteriaEvaluation> criteriaEvaluations = criteria.getCriteriaEvaluations();

                    Map<Integer, CriteriaEvaluation> criteriaEvalBySize = criteriaEvaluations.stream()
                            .collect(Collectors.toMap(CriteriaEvaluation::getTeamSize, ce -> ce));
                    int teamSize = team.getStudents().size();

                    CriteriaEvaluation criteriaEvaluation = criteriaEvalBySize.get(teamSize);

                    Map<String, Object> criteriaInfo = new HashMap<>();
                    criteriaInfo.put("criteriaName", criteria.getName());
                    criteriaInfo.put("pointsObtained", evaluation != null ? evaluation.getPoints() : null);
                    criteriaInfo.put("maxPoints", criteriaEvaluation.getPoints());
                    criteriaInfo.put("feedback", evaluation != null ? evaluation.getFeedback() : null);
                    criteriaInfo.put("criteriaId", criteria.getId());
                    criteriaInfo.put("type", criteria.getType());
                    criteriaInfo.put("evaluationId", evaluation != null ? evaluation.getId() : null);
                    criteriaInfo.put("teamId", team.getId());
                    criteriaInfo.put("studentId", null);

                    criteriaData.add(criteriaInfo);
                }
            }

            groupData.put("criteria", criteriaData);
            evaluationData.add(groupData);
        }

        return evaluationData;
    }

    /**
     * For the specified student, gets the individual evaluation results for each individual
     * criteria in the activity.
     *
     * @param activityId The ID of the activity for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @param studentId  The ID of the student for which data is being retrieved.
     *                   This value is required as a request parameter.
     * @return evaluationData containing the information to display in the table
     */
    public List<Map<String, Object>> getStudentEvaluationData(Long studentId, Long activityId) {
        List<Group> groups = groupRepository.findByActivityId(activityId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for id: " + studentId));
        List<Map<String, Object>> evaluationData = new ArrayList<>();
        for (Group group : groups) {
            List<Criteria> criteriaList = criteriaRepository.findByGroupId(group.getId());

            Map<String, Object> groupData = new HashMap<>();
            groupData.put("groupName", group.getName());

            List<Map<String, Object>> criteriaData = new ArrayList<>();
            for (Criteria criteria : criteriaList) {
                if (criteria.getType() == CriteriaType.INDIVIDUAL) {
                    IndividualEvaluation evaluation = individualEvaluationRepository.findByStudentIdAndCriteriaId(studentId, criteria.getId());
                    CriteriaEvaluation criteriaEvaluation = criteria.getCriteriaEvaluations().get(0);

                    Map<String, Object> criteriaInfo = new HashMap<>();
                    criteriaInfo.put("criteriaName", criteria.getName());
                    criteriaInfo.put("pointsObtained", evaluation != null ? evaluation.getPoints() : null);
                    criteriaInfo.put("maxPoints", criteriaEvaluation.getPoints());
                    criteriaInfo.put("feedback", evaluation != null ? evaluation.getFeedback() : null);
                    criteriaInfo.put("criteriaId", criteria.getId());
                    criteriaInfo.put("type", criteria.getType());
                    criteriaInfo.put("evaluationId", evaluation != null ? evaluation.getId() : null);
                    criteriaInfo.put("studentId", student.getId());
                    criteriaInfo.put("teamId", null);

                    criteriaData.add(criteriaInfo);
                }
            }

            groupData.put("criteria", criteriaData);
            evaluationData.add(groupData);
        }
        return evaluationData;
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
     * @return evaluationResult to render on the preview mail page
     */
    public EvaluationResult verifyEvaluations(String type, Long id, Long activityId, Long groupId) {
        EvaluationResult evaluationResult = new EvaluationResult();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));
        List<Criteria> criteria = criteriaRepository.findByGroupId(group.getId());
        List<Team> teams = teamRepository.findByActivityId(activityId);

        switch (type) {
            case "group":
                for (Criteria criterion : criteria) {
                    for (Team team : teams) {
                        evaluationResult = verifyEvaluationForTeam(evaluationResult, criterion, team);
                    }
                }
                break;

            case "team":
                Team team = teamRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Team not found."));
                for (Criteria criterion : criteria) {
                    evaluationResult = verifyEvaluationForTeam(evaluationResult, criterion, team);
                }
                break;

            case "student":
                Student student = studentRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Student not found."));
                Team teamStudent = teams.stream()
                        .filter(t -> t.getStudents().contains(student))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Team not found for student: " + id));

                for (Criteria criterion : criteria) {
                    if (criterion.getType() == CriteriaType.TEAM) {
                        TeamEvaluation teamEvaluation = teamEvaluationRepository.findByTeamIdAndCriteriaId(teamStudent.getId(), criterion.getId());
                        if (teamEvaluation == null || teamEvaluation.getPoints() == null) {
                            evaluationResult.getMissingEvaluations().add("Team: " + teamStudent.getName() + ", Criterion: " + criterion.getName());
                        } else {
                            evaluationResult.getCompletedTeamEval().add("Criterion name: " + criterion.getName() + " - Team name: " + teamStudent.getName() + " - Team points: " + teamEvaluation.getPoints() + " - Feedback: " + teamEvaluation.getFeedback());
                            evaluationResult.setTotalTeamPoints(evaluationResult.getTotalTeamPoints() + teamEvaluation.getPoints());
                        }
                    } else {
                        IndividualEvaluation individualEvaluation = individualEvaluationRepository.findByStudentIdAndCriteriaId(student.getId(), criterion.getId());

                        if (individualEvaluation == null || individualEvaluation.getPoints() == null) {
                            evaluationResult.getMissingEvaluations().add("Student: " + student.getFirstName() + " " + student.getLastName() + ", Criterion: " + criterion.getName());
                        } else {
                            evaluationResult.getCompletedIndEval().add("Criterion name: " + criterion.getName() + " - Student name: " + student.getFirstName() + " " + student.getLastName() + " - Individual points: " + individualEvaluation.getPoints() + " - Feedback: " + individualEvaluation.getFeedback());

                            int currentPoints = evaluationResult.getIndividualPointsMap().getOrDefault(student.getId(), 0);
                            evaluationResult.getIndividualPointsMap().put(student.getId(), currentPoints + individualEvaluation.getPoints());
                        }
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid type.");
        }

        // Return the complete evaluation result
        return evaluationResult;
    }

    /**
     * Function used to verifyEvaluation for a team, used in verifyEvaluations
     * @param evaluationResult
     * @param criterion
     * @param team
     * @return evaluationResult
     */
    private EvaluationResult verifyEvaluationForTeam(
            EvaluationResult evaluationResult,
            Criteria criterion,
            Team team) {

        if (criterion.getType() == CriteriaType.TEAM) {
            TeamEvaluation teamEvaluation = teamEvaluationRepository.findByTeamIdAndCriteriaId(team.getId(), criterion.getId());
            if (teamEvaluation == null || teamEvaluation.getPoints() == null) {
                evaluationResult.getMissingEvaluations().add("Team: " + team.getName() + ", Criterion: " + criterion.getName());
            } else {
                evaluationResult.getCompletedTeamEval().add("Criterion name: " + criterion.getName() + " - Team name: " + team.getName() + " - Team points: " + teamEvaluation.getPoints() + " - Feedback: " + teamEvaluation.getFeedback());
                evaluationResult.setTotalTeamPoints(evaluationResult.getTotalTeamPoints() + teamEvaluation.getPoints());
            }
        } else {
            for (Student student : team.getStudents()) {
                IndividualEvaluation individualEvaluation = individualEvaluationRepository.findByStudentIdAndCriteriaId(student.getId(), criterion.getId());

                if (individualEvaluation == null || individualEvaluation.getPoints() == null) {
                    evaluationResult.getMissingEvaluations().add("Student: " + student.getFirstName() + " " + student.getLastName() + ", Criterion: " + criterion.getName());
                } else {
                    evaluationResult.getCompletedIndEval().add("Criterion name: " + criterion.getName() + " - Student name: " + student.getFirstName() + " " + student.getLastName() + " - Individual points: " + individualEvaluation.getPoints() + " - Feedback: " + individualEvaluation.getFeedback());

                    if (evaluationResult.getIndividualPointsMap().containsKey(student.getId())) {
                        int currentPoints = evaluationResult.getIndividualPointsMap().get(student.getId());
                        evaluationResult.getIndividualPointsMap().put(student.getId(), currentPoints + individualEvaluation.getPoints());
                    } else {
                        evaluationResult.getIndividualPointsMap().put(student.getId(), individualEvaluation.getPoints());
                    }
                }
            }
        }

        return evaluationResult;
    }




    /**
     * Represents the response for a group evaluation, encapsulating the group's name
     * and a list of criteria associated with the evaluation.
     */
    public class GroupEvaluationResponse {
        private String groupName;
        private List<Criteria> criteria;

        public GroupEvaluationResponse(Group group, List<Criteria> criteria) {
            this.groupName = group.getName();
            this.criteria = criteria;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<Criteria> getCriteria() {
            return criteria;
        }

        public void setCriteria(List<Criteria> criteria) {
            this.criteria = criteria;
        }
    }

    /**
     * Encapsulates the informations used for updating an evaluation
     * The needed field are CriteriaType, criteriaId, teamId, studentId,
     * evaluationId, field(points or feedback) and value(new).
     */
    public static class EvaluationUpdateRequest {
        private Criteria.CriteriaType type;
        private Long criteriaId;
        private Long teamId;
        private Long studentId;
        private Long evaluationId;
        private String field;
        private String value;

        @Override
        public String toString() {
            return "EvaluationUpdateRequest{" + "type=" + type + ", criteriaId=" + criteriaId + ", teamId=" + teamId + ", studentId=" + studentId + ", evaluationId=" + evaluationId + ", field=" + field + ", value=" + value + '}';
        }

        public Criteria.CriteriaType getType() {
            return type;
        }

        public void setType(Criteria.CriteriaType type) {
            this.type = type;
        }

        public Long getCriteriaId() {
            return criteriaId;
        }

        public void setCriteriaId(Long criteriaId) {
            this.criteriaId = criteriaId;
        }

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public Long getEvaluationId() {
            return evaluationId;
        }

        public void setEvaluationId(Long evaluationId) {
            this.evaluationId = evaluationId;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * Encapsulates a criteria object with those three fields : id, name and type
     */
    public class CriteriaDTO {
        private Long id;
        private String name;
        private Criteria.CriteriaType type;

        public CriteriaDTO(Long id, String name, Criteria.CriteriaType type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Criteria.CriteriaType getType() {
            return type;
        }

        public void setType(Criteria.CriteriaType type) {
            this.type = type;
        }

    }

    public class EvaluationResult {
        private List<String> missingEvaluations;
        private List<String> completedTeamEval;
        private List<String> completedIndEval;
        private Integer totalTeamPoints;
        private Map<Long, Integer> individualPointsMap;

        // Constructor
        public EvaluationResult() {
            this.missingEvaluations = new ArrayList<>();
            this.completedTeamEval = new ArrayList<>();
            this.completedIndEval = new ArrayList<>();
            this.totalTeamPoints = 0;
            this.individualPointsMap = new HashMap<>();
        }

        // Getters and Setters
        public List<String> getMissingEvaluations() {
            return missingEvaluations;
        }

        public void setMissingEvaluations(List<String> missingEvaluations) {
            this.missingEvaluations = missingEvaluations;
        }

        public List<String> getCompletedTeamEval() {
            return completedTeamEval;
        }

        public void setCompletedTeamEval(List<String> completedTeamEval) {
            this.completedTeamEval = completedTeamEval;
        }

        public List<String> getCompletedIndEval() {
            return completedIndEval;
        }

        public void setCompletedIndEval(List<String> completedIndEval) {
            this.completedIndEval = completedIndEval;
        }

        public Integer getTotalTeamPoints() {
            return totalTeamPoints;
        }

        public void setTotalTeamPoints(Integer totalTeamPoints) {
            this.totalTeamPoints = totalTeamPoints;
        }

        public Map<Long, Integer> getIndividualPointsMap() {
            return individualPointsMap;
        }

        public void setIndividualPointsMap(Map<Long, Integer> individualPointsMap) {
            this.individualPointsMap = individualPointsMap;
        }
    }

}
