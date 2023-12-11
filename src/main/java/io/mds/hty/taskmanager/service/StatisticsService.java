package io.mds.hty.taskmanager.service;

import io.mds.hty.taskmanager.model.dao.Employee;
import io.mds.hty.taskmanager.model.dao.Task;
import io.mds.hty.taskmanager.model.dto.CommonStatDto;
import io.mds.hty.taskmanager.model.dto.PeriodDto;
import io.mds.hty.taskmanager.model.dto.UserStatsDto;
import io.mds.hty.taskmanager.repo.EmployeeRepo;
import io.mds.hty.taskmanager.repo.TaskGroupRepo;
import io.mds.hty.taskmanager.repo.TaskRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class StatisticsService {

    @Autowired
    private TaskGroupRepo taskGroupRepo;
    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public CommonStatDto getCommonStat(PeriodDto period) {
        Long emplCount = employeeRepo.count();
        Map<Employee.Role, Long> countByRole = employeeRepo.findAll().stream()
                .flatMap(e -> e.getRoles().stream())
                .collect(Collectors.groupingBy(r -> r, Collectors.counting()));
        Long taskCount = taskRepo.count();

        Long finTaskCount = taskRepo.countAllByStatusIs(Task.TaskStatus.FINISH_APPROVED);
        Long notFinTaskCount = taskRepo.countAllByStatusIsNot(Task.TaskStatus.FINISH_APPROVED);
        Long tgCount = taskGroupRepo.count();

        Double avgTasksAsOnE = employeeRepo.getAverageTasksAssignedOnEmployee(period.getStart(), period.getEnd());
        Double avgTasksCrOnE = employeeRepo.getAverageTasksCreateOnEmployee(period.getStart(), period.getEnd());
        Double avgFinTasksAsOnE = employeeRepo.getAverageFinishedTasksAssignedOnEmployee(period.getStart(), period.getEnd());
        PeriodDto[] periods = taskRepo.getPeriodsOfFinishedTasksAssignedOnEmployee(period.getStart(), period.getEnd());
        Double avgPeriods = Arrays.stream(periods)
                .filter(per -> per.getStart() != null && per.getEnd() != null)
                .mapToDouble(p -> Period.between(p.getStart().atZone(ZoneId.systemDefault()).toLocalDate(),
                        p.getEnd().atZone(ZoneId.systemDefault()).toLocalDate()).getDays()).average().orElse(-1.0);

        return CommonStatDto.builder().period(period).employeeCount(emplCount).byRole(countByRole).taskCount(taskCount)
                .finishedTasksCount(finTaskCount).notFinishedTasksCount(notFinTaskCount).taskGroupCount(tgCount)
                .averageTasksAssignedOnEmployee(avgTasksAsOnE).averageTasksOnEmployeeCreated(avgTasksCrOnE)
                .averageFinishedTasksOnEmployee(avgFinTasksAsOnE).averageTimeOnTasksWorkingDays(avgPeriods).build();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public UserStatsDto getUserStat(Employee principal, String userName, PeriodDto p) throws AccessDeniedException {
        entityManager.refresh(principal);
        if(!principal.getRoles().contains(Employee.Role.ADMIN) && !principal.getRoles().contains(Employee.Role.TEAMLEAD)
                && !principal.getUsername().equals(userName)){
            throw new AccessDeniedException("Forbidden");
        }
        Employee user = employeeRepo.findEmployeeByUserNameIs(userName).orElseThrow(() -> new UsernameNotFoundException("User with Username " + userName + " not found"));
        if(!principal.getRoles().contains(Employee.Role.ADMIN)
                && principal.getTaskGroups().stream().noneMatch(tg -> user.getTaskGroups().contains(tg))){
            throw new AccessDeniedException("Forbidden");
        }
        Stream<Task> tasksStream = user.getTasksAssigned().stream();
        Long tInWorkNow = tasksStream
                .filter(t -> !Task.TaskStatus.CREATED.equals(t.getStatus()) && !Task.TaskStatus.FINISH_APPROVED.equals(t.getStatus()))
                .count();
        Long tStartedInP = tasksStream
                .filter(t -> t.getStarted() != null && t.getStarted().isAfter(p.getStart()) && t.getStarted().isBefore(p.getEnd()))
                .count();
        Long tCompleteInP = tasksStream
                .filter(t -> t.getCompleted() != null && t.getCompleted().isAfter(p.getStart()) && t.getCompleted().isBefore(p.getEnd()))
                .count();
        Long tStartAndCompleteInP = tasksStream
                .filter(t -> t.getStarted() != null && t.getCompleted() != null && t.getStarted().isAfter(p.getStart()) && t.getCompleted().isBefore(p.getEnd()))
                .count();
        Double avgPriority = tasksStream
                .filter(t -> t.getStarted() != null && t.getCompleted() != null && t.getStarted().isAfter(p.getStart()) && t.getCompleted().isBefore(p.getEnd()))
                .mapToDouble(Task::getPriority).average().orElse(-1.0);
        Double avgCountDaysOnT = tasksStream
                .filter(t -> t.getStarted() != null && t.getCompleted() != null && t.getStarted().isAfter(p.getStart()) && t.getCompleted().isBefore(p.getEnd()))
                .mapToDouble(t -> Period.between(t.getStarted().atZone(ZoneId.systemDefault()).toLocalDate(),
                        t.getCompleted().atZone(ZoneId.systemDefault()).toLocalDate()).getDays()).average().orElse(-1.0);
        Double avgComplexity = tasksStream
                .filter(t -> t.getStarted() != null && t.getCompleted() != null && t.getStarted().isAfter(p.getStart()) && t.getCompleted().isBefore(p.getEnd()))
                .mapToDouble(Task::getComplexity).average().orElse(-1.0);
        Double efficiency = avgComplexity * 100 / avgCountDaysOnT;
        Double value = avgPriority * efficiency;
        return UserStatsDto.builder().period(p).tasksInWorkForNow(tInWorkNow).tasksStartedInPeriod(tStartedInP).tasksFinishedInPeriod(tCompleteInP)
                .tasksStartedAndFinishedInPeriod(tStartAndCompleteInP).averageCountDaysOnTask(avgCountDaysOnT).averageTasksPriority(avgPriority)
                .averageTasksComplexity(avgComplexity).efficiency(efficiency).value(value).build();
    }
}
