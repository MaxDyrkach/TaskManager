package io.mds.hty.taskmanager.controller;


import io.mds.hty.taskmanager.common.Action;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dto.*;
import io.mds.hty.taskmanager.service.StatisticsService;
import io.mds.hty.taskmanager.service.TaskGroupService;
import io.mds.hty.taskmanager.service.TaskService;
import io.mds.hty.taskmanager.service.UserDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping(path = "${api.teamleadPrefix}", produces = "application/json")
@PreAuthorize("hasRole('ROLE_TEAMLEAD')")
public class TeamleadController {

    @Autowired
    UserDataService userDataService;
    @Autowired
    TaskService taskService;
    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/group/byid/{id}/developers")
    public ResponseEntity<?> getGroupDevsData(@Valid @PathVariable Long id) throws AccessDeniedException {
            Authentication authentication = getAuthentication();
            Employee e = (Employee) authentication.getPrincipal();
            return ResponseEntity
                    .ok(userDataService.getGroupDevelopersData(e, id));
    }

    @GetMapping("/getdev/byuname/{uname}")
    public ResponseEntity<?> getOtherDevDataInSameGroups(@Valid @PathVariable("uname") String userName) throws AccessDeniedException {
            Authentication authentication = getAuthentication();
            Employee e = (Employee) authentication.getPrincipal();
            return ResponseEntity
                    .of(userDataService.getUserDataByEmployeeGroups(e, userName));
    }

    @PostMapping(value = "/data/edit", consumes = "application/json")
    public ResponseEntity<String> editSelfTeamleadData(@Valid @RequestBody EmployeeDto employeeDto) throws AccessDeniedException {
            Authentication auth = getAuthentication();
            Employee e = (Employee) auth.getPrincipal();
            if (!e.getUsername().equals(employeeDto.getUserName())) throw new AccessDeniedException("Unauthorized");
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(userDataService.editSelfTeamlead(employeeDto));
    }

    @PostMapping("/group/createorupdate")
    public ResponseEntity<?> createOrUpdateGroupByName(@Valid @RequestBody TaskGroupDto dto) throws AccessDeniedException {
            if (dto.getName() == null || dto.getName().isBlank())
                throw new IllegalArgumentException("Group name must be provided");
            Authentication auth = getAuthentication();
            Employee e = (Employee) auth.getPrincipal();
            return ResponseEntity.ok(taskGroupService.createOrUpdateGroup(dto, e));
    }

    @PostMapping("/group/delete")
    public ResponseEntity<?> deleteGroupByIdOrName(@RequestBody TaskGroupDto tgDto) {
            if (tgDto.getName() == null || tgDto.getName().isBlank() || tgDto.getId() == null)
                throw new IllegalArgumentException("Group name or id must be provided");
            return ResponseEntity.ok(taskGroupService.deleteGroupByIdOrName(tgDto));
    }

    @PostMapping("/dev/addordelgroup")
    public ResponseEntity<?> addOrDeleteDeveloperGroup(@Valid @RequestBody EmlpoyeeGroupActionDto dto) {
        try {
            Authentication auth = getAuthentication();
            Employee e = (Employee) auth.getPrincipal();
            return ResponseEntity.ok(userDataService.addOrDeleteUserGroup(e, dto));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (UsernameNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/task/{action}")
    public ResponseEntity<?> createUpdateDeleteTaskById(@Valid @RequestBody TaskDto dto, @Valid @RequestParam String action) throws AccessDeniedException {
            Action act = Action.valueOf(action.toUpperCase());
            if (dto.getId() == null) throw new IllegalArgumentException("Task id must be provided");
            Authentication auth = getAuthentication();
            Employee e = (Employee) auth.getPrincipal();
            return ResponseEntity.ok(taskService.taskCud(e, dto, act));
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks() throws AccessDeniedException {
            Authentication auth = getAuthentication();
            Employee principal = (Employee) auth.getPrincipal();
            return ResponseEntity.ok(taskService.getTasksByGroupNames(principal));
    }

    @GetMapping("/stats/common")
    public ResponseEntity<CommonStatDto> getCommonStats(@Valid @RequestBody PeriodDto p) {
        return ResponseEntity.ok(statisticsService.getCommonStat(p));
    }

    @GetMapping("/stats/user/{name}")
    public ResponseEntity<?> getUserStat(@Valid @PathVariable String name, @Valid @RequestBody PeriodDto p) throws AccessDeniedException {
            Authentication auth = getAuthentication();
            Employee principal = (Employee) auth.getPrincipal();
            return ResponseEntity.ok(statisticsService.getUserStat(principal,name, p));
    }

    private Authentication getAuthentication() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Principal is null");
        }
        return authentication;
    }
}
