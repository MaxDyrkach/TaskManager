package io.mds.hty.taskmanager.controller;

import io.mds.hty.taskmanager.model.dto.Action;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dto.*;
import io.mds.hty.taskmanager.service.StatisticsService;
import io.mds.hty.taskmanager.service.TaskService;
import io.mds.hty.taskmanager.service.UserDataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@PreAuthorize("hasAnyRole('ROLE_SENIOR_DEVELOPER', 'ROLE_MIDDLE_DEVELOPER', 'ROLE_JUNIOR_DEVELOPER')")
@RequestMapping(path = "${api.devPrefix}", produces = "application/json")
public class DeveloperController {


    @Autowired
    private UserDataService userDataService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private StatisticsService statisticsService;


    @GetMapping("/data")
    public ResponseEntity<Employee> getSelfData() throws AccessDeniedException {
        Authentication authentication = getAuthentication();
        return ResponseEntity
                .of(userDataService.getEmployeeData(authentication.getName()));
    }


    @PostMapping(value = "/data/{uname}", consumes = "application/json")
    public ResponseEntity<String> editSelfData(@Valid @RequestBody EmployeeDto employeeDto,
                                               @Valid @PathVariable String uname) throws AccessDeniedException {
        Authentication auth = getAuthentication();
        Employee e = (Employee) auth.getPrincipal();
        if (!e.getUsername().equals(employeeDto.getUserName()))
            throw new AccessDeniedException("You may see your data only");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(userDataService.editSelfDeveloper(uname, employeeDto));

    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getEmployeeTasks() throws AccessDeniedException {
        Authentication authentication = getAuthentication();
        return ResponseEntity.ok(userDataService.getUserTasksVerbose(authentication.getName(), "Assigned"));
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> getEmployeeFinishedTasks( @RequestBody Boolean finished) throws AccessDeniedException {
        Authentication authentication = getAuthentication();
        Employee e = (Employee) authentication.getPrincipal();
        return ResponseEntity
                .ok(taskService.getEmployeeFinishedTasks(e, finished));
    }

    @GetMapping("/task/byid/{id}")
    public ResponseEntity<?> getEmployeeTaskById(@Valid @NotNull @PathVariable("id") Long id) throws AccessDeniedException {
        Authentication authentication = getAuthentication();
        Employee e = (Employee) authentication.getPrincipal();
        return ResponseEntity.ok(taskService.getEmployeeTaskById(e.getId(), id)
                .orElseThrow(() -> new AccessDeniedException("No such task")));
    }

    @PostMapping(value = "/task/update", consumes = "application/json")
    public ResponseEntity<String> updateEmployeeTask(@Valid @RequestBody TaskDto taskDto) throws AccessDeniedException {
        Long id = taskDto.getId();
        String status = taskDto.getStatus();
        Integer progress = taskDto.getProgress();
        Task.TaskStatus st = Task.TaskStatus.valueOf(status);
        Authentication authentication = getAuthentication();
        Employee e = (Employee) authentication.getPrincipal();
        if ((!e.getRoles().contains(Employee.Role.TEAMLEAD))
                && (st.equals(Task.TaskStatus.CREATED) || st.equals(Task.TaskStatus.ASSIGNED) || st.equals(Task.TaskStatus.FINISH_APPROVED))) {
            throw new AccessDeniedException("Cannot change to given status");
        }
        if (taskService.updateEmployeeTask(e, id, st, progress).isPresent()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("UPDATED");
        }
        throw new AccessDeniedException("Unauthorized");
    }

    @PostMapping(value = "/comment/leave", consumes = "application/json")
    public ResponseEntity<String> leaveCommentOnTask(@Valid @RequestBody CommentDto commentDto) throws AccessDeniedException, MethodArgumentNotValidException {

        Authentication authentication = getAuthentication();
            Employee e = (Employee) authentication.getPrincipal();
            return ResponseEntity.ok(taskService.commentCud(e, commentDto, Action.NEW));
    }

    @PostMapping(value = "/comment/edit", consumes = "application/json")
    public ResponseEntity<String> editCommentOnTask(@Valid @RequestBody CommentDto commentDto) throws AccessDeniedException {
            Authentication authentication = getAuthentication();
            Employee e = (Employee) authentication.getPrincipal();
            return ResponseEntity.ok(taskService.commentCud(e, commentDto, Action.EDIT));

    }

    @PostMapping(value = "/comment/delete", consumes = "application/json")
    public ResponseEntity<String> deleteCommentOnTask(@RequestBody CommentDto commentDto) throws AccessDeniedException {
            Authentication authentication = getAuthentication();
            Employee e = (Employee) authentication.getPrincipal();
            return ResponseEntity.ok(taskService.commentCud(e, commentDto, Action.DELETE));
    }

    @GetMapping("/task/byid/{id}/comments")
    public ResponseEntity<?> getEmployeeTaskByIdComments(@Valid @PathVariable Long id) throws AccessDeniedException {
            Authentication authentication = getAuthentication();
            Employee e = (Employee) authentication.getPrincipal();
            return ResponseEntity.ok(taskService.getEmployeeTaskById(e.getId(), id)
                    .orElseThrow(() -> new AccessDeniedException("No such task")).getComments());
    }

    @GetMapping("/stats/common")
    public ResponseEntity<CommonStatDto> getCommonStats(@Valid @RequestBody PeriodDto p) {
        return ResponseEntity.ok(statisticsService.getCommonStat(p));
    }

    @GetMapping("/stats/self")
    public ResponseEntity<?> getUserStat(@RequestBody PeriodDto p) throws AccessDeniedException {
            Authentication auth = getAuthentication();
            Employee principal = (Employee) auth.getPrincipal();
            return ResponseEntity.ok(statisticsService.getUserStat(principal, principal.getUsername(), p));
    }

    private Authentication getAuthentication() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Principal is null");
        }
        return authentication;
    }


}
