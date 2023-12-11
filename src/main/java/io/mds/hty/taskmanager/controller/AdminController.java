package io.mds.hty.taskmanager.controller;

import io.mds.hty.taskmanager.model.dto.Action;
import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dto.CommonStatDto;
import io.mds.hty.taskmanager.model.dto.EmployeeDto;
import io.mds.hty.taskmanager.model.dto.PeriodDto;
import io.mds.hty.taskmanager.model.dto.UserStatsDto;
import io.mds.hty.taskmanager.service.StatisticsService;
import io.mds.hty.taskmanager.service.UserDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping(path = "${api.adminPrefix}", produces = "application/json")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired
    UserDataService userDataService;
    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/users")
    public ResponseEntity<List<Employee>> getAllUsers() {
        return ResponseEntity.ok(userDataService.getAllEmployees());
    }

    @PostMapping(value = "/user/{action}", consumes = "application/json")
    public ResponseEntity<?> createOrEditAnyUserData(@Valid @RequestBody EmployeeDto employeeDto,
                                                     @Valid @PathVariable String action) {
        Action act = Action.valueOf(action.toUpperCase());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(userDataService.editAnyEmployeeDataByUserName(employeeDto, act));
    }

    @GetMapping("/stats/common")
    public ResponseEntity<CommonStatDto> getCommonStats(@RequestBody PeriodDto p) {
        return ResponseEntity.ok(statisticsService.getCommonStat(p));
    }

    @GetMapping("/stats/user/{name}")
    public ResponseEntity<UserStatsDto> getUserStat(@PathVariable String name, @RequestBody PeriodDto p) throws AccessDeniedException {
        Authentication auth = getAuthentication();
        Employee principal = (Employee) auth.getPrincipal();
        return ResponseEntity.ok(statisticsService.getUserStat(principal, name, p));
    }

    private Authentication getAuthentication() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Principal is null");
        }
        return authentication;
    }


}
